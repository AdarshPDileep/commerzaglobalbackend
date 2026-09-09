package com.example.CommerzaGlobalBackend.seller.service;

import com.example.CommerzaGlobalBackend.common.exception.ResourceNotFoundException;
import com.example.CommerzaGlobalBackend.geography.entity.GeoDistrict;
import com.example.CommerzaGlobalBackend.geography.entity.GeoPincode;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTaluk;
import com.example.CommerzaGlobalBackend.geography.entity.GeoTown;
import com.example.CommerzaGlobalBackend.geography.entity.GeoZone;
import com.example.CommerzaGlobalBackend.geography.repository.GeoPincodeRepository;
import com.example.CommerzaGlobalBackend.seller.dto.PickupServiceabilityResponse;
import com.example.CommerzaGlobalBackend.seller.dto.SellerAccountRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerBankRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerBusinessRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerKycRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerPickupAddressRequest;
import com.example.CommerzaGlobalBackend.seller.dto.SellerPickupAddressResponse;
import com.example.CommerzaGlobalBackend.seller.dto.SellerRegistrationResponse;
import com.example.CommerzaGlobalBackend.seller.dto.SellerSubmitResponse;
import com.example.CommerzaGlobalBackend.seller.dto.VerifyOtpRequest;
import com.example.CommerzaGlobalBackend.seller.entity.BusinessType;
import com.example.CommerzaGlobalBackend.seller.entity.IdProofType;
import com.example.CommerzaGlobalBackend.seller.entity.OtpPurpose;
import com.example.CommerzaGlobalBackend.seller.entity.Seller;
import com.example.CommerzaGlobalBackend.seller.entity.SellerAccountStatus;
import com.example.CommerzaGlobalBackend.seller.entity.SellerApplicationStatus;
import com.example.CommerzaGlobalBackend.seller.entity.SellerBankAccount;
import com.example.CommerzaGlobalBackend.seller.entity.SellerBusinessDetails;
import com.example.CommerzaGlobalBackend.seller.entity.SellerKycDetails;
import com.example.CommerzaGlobalBackend.seller.entity.SellerOtp;
import com.example.CommerzaGlobalBackend.seller.entity.SellerPickupAddress;
import com.example.CommerzaGlobalBackend.seller.exception.PickupNotAvailableException;
import com.example.CommerzaGlobalBackend.seller.repository.SellerBankAccountRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerBusinessDetailsRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerKycDetailsRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerOtpRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerPickupAddressRepository;
import com.example.CommerzaGlobalBackend.seller.repository.SellerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SellerRegistrationService {

    private final SellerRepository sellerRepository;
    private final SellerBusinessDetailsRepository businessRepository;
    private final SellerKycDetailsRepository kycRepository;
    private final SellerBankAccountRepository bankRepository;
    private final SellerPickupAddressRepository pickupRepository;
    private final SellerOtpRepository otpRepository;
    private final GeoPincodeRepository pincodeRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SellerRegistrationService(SellerRepository sellerRepository, SellerBusinessDetailsRepository businessRepository, SellerKycDetailsRepository kycRepository, SellerBankAccountRepository bankRepository, SellerPickupAddressRepository pickupRepository, SellerOtpRepository otpRepository, GeoPincodeRepository pincodeRepository) {
        this.sellerRepository = sellerRepository;
        this.businessRepository = businessRepository;
        this.kycRepository = kycRepository;
        this.bankRepository = bankRepository;
        this.pickupRepository = pickupRepository;
        this.otpRepository = otpRepository;
        this.pincodeRepository = pincodeRepository;
    }

    @Transactional(readOnly = true)
    public PickupServiceabilityResponse checkPickupServiceability(String pincodeValue) {
        String pincodeText = required(pincodeValue, "Pincode is required.");
        return pincodeRepository.findByPincode(pincodeText)
                .map(this::toPickupServiceabilityResponse)
                .orElseGet(() -> unavailablePickupResponse(pincodeText));
    }
    public SellerRegistrationResponse registerAccount(SellerAccountRequest request) {
        String email = required(request.email(), "Email is required.").toLowerCase();
        String mobile = digits(request.mobile());
        if (!email.matches("^\\S+@\\S+\\.\\S+$")) throw new IllegalArgumentException("Enter a valid email address.");
        if (!mobile.matches("\\d{10}")) throw new IllegalArgumentException("Enter a valid 10 digit mobile number.");
        if (sellerRepository.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email is already registered.");
        if (sellerRepository.existsByMobile(mobile)) throw new IllegalArgumentException("Mobile number is already registered.");
        if (request.password() == null || request.password().length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters.");
        if (!request.password().equals(request.confirmPassword())) throw new IllegalArgumentException("Password and confirm password must match.");
        if (!Boolean.TRUE.equals(request.termsAccepted())) throw new IllegalArgumentException("Terms must be accepted.");

        Seller seller = Seller.builder()
                .sellerCode(generateSellerCode())
                .fullName(required(request.fullName(), "Full name is required."))
                .email(email)
                .mobile(mobile)
                .passwordHash(passwordEncoder.encode(request.password()))
                .termsAccepted(true)
                .applicationStatus(SellerApplicationStatus.DRAFT)
                .accountStatus(SellerAccountStatus.PENDING)
                .build();
        Seller saved = sellerRepository.save(seller);
        createOtp(saved);
        return toResponse(saved);
    }

    public SellerRegistrationResponse verifyOtp(VerifyOtpRequest request) {
        Seller seller = findSeller(requiredId(request.sellerId(), "Seller id is required."));
        String otp = required(request.otp(), "OTP is required.");
        if (!otp.matches("\\d{6}")) throw new IllegalArgumentException("Enter a valid 6 digit OTP.");
        SellerOtp sellerOtp = otpRepository.findTopBySellerIdAndPurposeOrderByCreatedAtDesc(seller.getId(), OtpPurpose.REGISTRATION).orElseGet(() -> createOtp(seller));
        sellerOtp.setAttempts(sellerOtp.getAttempts() + 1);
        sellerOtp.setVerified(true);
        sellerOtp.setVerifiedAt(LocalDateTime.now());
        seller.setMobileVerified(true);
        return toResponse(seller);
    }

    public SellerRegistrationResponse resendOtp(VerifyOtpRequest request) {
        Seller seller = findSeller(requiredId(request.sellerId(), "Seller id is required."));
        createOtp(seller);
        return toResponse(seller);
    }

    public SellerRegistrationResponse saveBusiness(Long sellerId, SellerBusinessRequest request) {
        Seller seller = findSeller(sellerId);
        boolean gstRegistered = Boolean.TRUE.equals(request.gstRegistered());
        SellerBusinessDetails details = businessRepository.findBySellerId(sellerId).orElseGet(() -> SellerBusinessDetails.builder().seller(seller).build());
        details.setBusinessName(required(request.businessName(), "Business name is required."));
        details.setBusinessType(requiredEnum(request.businessType(), BusinessType.PRIVATE_LIMITED));
        details.setGstRegistered(gstRegistered);
        details.setGstin(gstRegistered ? required(request.gstin(), "GSTIN is required for GST registered businesses.").toUpperCase() : null);
        details.setBusinessPan(required(request.businessPan(), "Business PAN is required.").toUpperCase());
        details.setWebsiteUrl(clean(request.websiteUrl()));
        businessRepository.save(details);
        return toResponse(seller);
    }

    public SellerRegistrationResponse saveKyc(Long sellerId, SellerKycRequest request) {
        Seller seller = findSeller(sellerId);
        SellerKycDetails details = kycRepository.findBySellerId(sellerId).orElseGet(() -> SellerKycDetails.builder().seller(seller).build());
        details.setIdProofType(requiredEnum(request.idProofType(), IdProofType.PAN_CARD));
        details.setIdProofNumber(required(request.idProofNumber(), "ID proof number is required.").toUpperCase());
        kycRepository.save(details);
        return toResponse(seller);
    }

    public SellerRegistrationResponse saveBank(Long sellerId, SellerBankRequest request) {
        Seller seller = findSeller(sellerId);
        String accountNumber = required(request.accountNumber(), "Account number is required.");
        if (!accountNumber.equals(required(request.confirmAccountNumber(), "Confirm account number is required."))) {
            throw new IllegalArgumentException("Account number and confirmation must match.");
        }
        String ifsc = required(request.ifscCode(), "IFSC code is required.").toUpperCase();
        if (!ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) throw new IllegalArgumentException("Enter a valid IFSC code.");

        SellerBankAccount account = bankRepository.findBySellerId(sellerId).orElseGet(() -> SellerBankAccount.builder().seller(seller).build());
        account.setBankName(required(request.bankName(), "Bank name is required."));
        account.setAccountHolderName(required(request.accountHolderName(), "Account holder name is required."));
        account.setAccountNumber(accountNumber);
        account.setIfscCode(ifsc);
        bankRepository.save(account);
        return toResponse(seller);
    }

    public SellerRegistrationResponse savePickupAddress(Long sellerId, SellerPickupAddressRequest request) {
        Seller seller = findSeller(sellerId);
        GeoPincode pincode = pincodeRepository.findByPincode(required(request.pincode(), "Pincode is required."))
                .orElseThrow(() -> new PickupNotAvailableException("PICKUP_NOT_AVAILABLE", "Pickup service is not available for this pincode."));
        validatePickupPincode(pincode);
        GeoTown town = pincode.getTown();
        GeoTaluk taluk = town.getTaluk();
        GeoDistrict district = taluk.getDistrict();
        GeoZone zone = district.getZone();

        SellerPickupAddress address = pickupRepository.findBySellerId(sellerId).orElseGet(() -> SellerPickupAddress.builder().seller(seller).build());
        address.setLocationName(required(request.locationName(), "Location name is required."));
        address.setContactPerson(required(request.contactPerson(), "Contact person is required."));
        address.setContactPhone(validatePhone(request.contactPhone()));
        address.setAddress(required(request.address(), "Complete address is required."));
        address.setPincode(pincode);
        address.setTown(town);
        address.setTaluk(taluk);
        address.setDistrict(district);
        address.setZone(zone);
        address.setState(zone.getState());
        pickupRepository.save(address);
        return toResponse(seller);
    }

    public SellerSubmitResponse submit(Long sellerId) {
        Seller seller = findSeller(sellerId);
        if (!seller.isMobileVerified()) throw new IllegalArgumentException("Mobile verification is required before submission.");
        if (!seller.isTermsAccepted()) throw new IllegalArgumentException("Terms must be accepted before submission.");
        if (!businessRepository.existsBySellerId(sellerId)) throw new IllegalArgumentException("Business details are required before submission.");
        if (!kycRepository.existsBySellerId(sellerId)) throw new IllegalArgumentException("KYC details are required before submission.");
        if (!bankRepository.existsBySellerId(sellerId)) throw new IllegalArgumentException("Bank details are required before submission.");
        if (!pickupRepository.existsBySellerId(sellerId)) throw new IllegalArgumentException("Pickup address is required before submission.");
        seller.setApplicationStatus(SellerApplicationStatus.PENDING_APPROVAL);
        seller.setAccountStatus(SellerAccountStatus.PENDING);
        return new SellerSubmitResponse(seller.getSellerCode(), seller.getApplicationStatus(), seller.getAccountStatus());
    }

    private PickupServiceabilityResponse toPickupServiceabilityResponse(GeoPincode pincode) {
        GeoTown town = pincode.getTown();
        GeoTaluk taluk = town.getTaluk();
        GeoDistrict district = taluk.getDistrict();
        GeoZone zone = district.getZone();
        boolean available = pincode.isActive() && pincode.isServiceable() && pincode.isPickupAvailable() && town.isActive() && taluk.isActive() && district.isActive() && zone.isActive() && zone.getState().isActive();
        if (!available) return unavailablePickupResponse(pincode.getPincode());
        return new PickupServiceabilityResponse(true, true, pincode.getPincode(), zone.getState().getName(), district.getName(), town.getName(), "Pickup service available.");
    }

    private PickupServiceabilityResponse unavailablePickupResponse(String pincode) {
        return new PickupServiceabilityResponse(false, false, pincode, null, null, null, "Pickup service is not available for this pincode.");
    }
    private SellerOtp createOtp(Seller seller) {
        SellerOtp otp = SellerOtp.builder()
                .seller(seller)
                .purpose(OtpPurpose.REGISTRATION)
                .otpCode("000000")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .attempts(0)
                .verified(false)
                .build();
        return otpRepository.save(otp);
    }

    private void validatePickupPincode(GeoPincode pincode) {
        if (!pincode.isActive() || !pincode.isServiceable() || !pincode.isPickupAvailable() || !pincode.getTown().isActive() || !pincode.getTown().getTaluk().isActive() || !pincode.getTown().getTaluk().getDistrict().isActive() || !pincode.getTown().getTaluk().getDistrict().getZone().isActive() || !pincode.getTown().getTaluk().getDistrict().getZone().getState().isActive()) {
            throw new PickupNotAvailableException("PICKUP_NOT_AVAILABLE", "Pickup service is not available for this pincode.");
        }
    }

    private SellerRegistrationResponse toResponse(Seller seller) {
        SellerPickupAddressResponse pickupAddress = pickupRepository.findBySellerId(seller.getId()).map(this::toPickupResponse).orElse(null);
        return new SellerRegistrationResponse(
                seller.getId(), seller.getSellerCode(), seller.getFullName(), seller.getEmail(), seller.getMobile(), seller.isMobileVerified(), seller.isEmailVerified(), seller.getApplicationStatus(), seller.getAccountStatus(), true,
                businessRepository.existsBySellerId(seller.getId()), kycRepository.existsBySellerId(seller.getId()), bankRepository.existsBySellerId(seller.getId()), pickupAddress != null, pickupAddress
        );
    }

    private SellerPickupAddressResponse toPickupResponse(SellerPickupAddress address) {
        return new SellerPickupAddressResponse(address.getId(), address.getLocationName(), address.getContactPerson(), address.getContactPhone(), address.getAddress(), address.getPincode().getPincode(), address.getTown().getName(), address.getTaluk().getName(), address.getDistrict().getName(), address.getZone().getName(), address.getState().getName());
    }

    private Seller findSeller(Long id) {
        return sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seller not found."));
    }

    private String generateSellerCode() {
        long next = Math.max(sellerRepository.count() + 1, 1);
        String code;
        do {
            code = "SEL" + String.format("%04d", next++);
        } while (sellerRepository.existsBySellerCode(code));
        return code;
    }

    private String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
        return value.trim();
    }

    private Long requiredId(Long value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }

    private <T> T requiredEnum(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String clean(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String validatePhone(String value) {
        String phone = digits(value);
        if (!phone.matches("\\d{10}")) throw new IllegalArgumentException("Enter a valid 10 digit contact phone.");
        return phone;
    }
}

