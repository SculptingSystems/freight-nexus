package com.freightnexus.contract;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ForbiddenException;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.partner.Partner;
import com.freightnexus.partner.PartnerRepository;
import com.freightnexus.partner.PartnerType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ContractService {

    private final ShipperContractRepository contractRepository;
    private final PartnerRepository partnerRepository;

    public ContractService(ShipperContractRepository contractRepository,
                           PartnerRepository partnerRepository) {
        this.contractRepository = contractRepository;
        this.partnerRepository = partnerRepository;
    }

    public List<ContractDTO.Response> findMine() {
        return contractRepository.findByPartnerId(caller().id()).stream().map(this::toResponse).toList();
    }

    public ContractDTO.Response findById(Long id) {
        return toResponse(load(id));
    }

    @Transactional
    public ContractDTO.Response create(ContractDTO.Request request) {
        FreightPrincipal me = caller();
        Partner carrier = partnerRepository.findById(me.id())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + me.id()));
        if (carrier.getType() != PartnerType.CARRIER) {
            throw new ForbiddenException("Only CARRIER partners can create contracts");
        }
        Partner shipper = partnerRepository.findById(request.shipperId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found: " + request.shipperId()));
        if (shipper.getType() != PartnerType.SHIPPER) {
            throw new IllegalArgumentException("Target partner must be a SHIPPER");
        }
        ShipperContract contract = new ShipperContract();
        contract.setCarrier(carrier);
        contract.setShipper(shipper);
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractDTO.Response activate(Long id) {
        ShipperContract contract = load(id);
        assertIsCarrier(contract);
        transition(contract, ContractStatus.ACTIVE);
        return toResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractDTO.Response terminate(Long id) {
        ShipperContract contract = load(id);
        assertIsCarrier(contract);
        transition(contract, ContractStatus.TERMINATED);
        return toResponse(contractRepository.save(contract));
    }

    private void transition(ShipperContract c, ContractStatus next) {
        boolean valid = switch (c.getStatus()) {
            case DRAFT  -> next == ContractStatus.ACTIVE || next == ContractStatus.TERMINATED;
            case ACTIVE -> next == ContractStatus.TERMINATED;
            case TERMINATED -> false;
        };
        if (!valid) throw new IllegalStateException(
                "Cannot transition contract from " + c.getStatus() + " to " + next);
        c.setStatus(next);
    }

    private void assertIsCarrier(ShipperContract c) {
        if (!c.getCarrier().getId().equals(caller().id())) {
            throw new ForbiddenException("Only the contract's carrier can modify it");
        }
    }

    private ShipperContract load(Long id) {
        return contractRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ContractDTO.Response toResponse(ShipperContract c) {
        return new ContractDTO.Response(c.getId(),
                c.getCarrier().getId(), c.getCarrier().getName(),
                c.getShipper().getId(), c.getShipper().getName(),
                c.getStatus(), c.getStartDate(), c.getEndDate(), c.getCreatedAt());
    }
}
