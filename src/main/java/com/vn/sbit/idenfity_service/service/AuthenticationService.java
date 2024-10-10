package com.vn.sbit.idenfity_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vn.sbit.idenfity_service.dto.request.AuthenticationRequest;
import com.vn.sbit.idenfity_service.dto.request.IntrospectRequest;
import com.vn.sbit.idenfity_service.dto.response.AuthenticationResponse;
import com.vn.sbit.idenfity_service.dto.response.IntrospectResponse;
import com.vn.sbit.idenfity_service.exception.AppException;
import com.vn.sbit.idenfity_service.exception.ErrorCode;
import com.vn.sbit.idenfity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    UserRepository userRepository;

    //kiểm tra token có hợp lệ hay không
    public IntrospectResponse introspectResponse(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        SignedJWT signedJWT = SignedJWT.parse(token);//Phân tích cú pháp chuỗi token thành một đối tượng SignedJWT.

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());// Private key sử dụng để tái tạo lại signature mới(dùng để so sánh với signature token).

        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();//Lấy thời gian hết hạn của token từ claims set của JWT.

        var verified=signedJWT.verify(verifier);//trả về true nếu token signature giống với signature được tạo để kiển tra

        return IntrospectResponse.builder()
                .valid(verified && expiryTime.after(new Date()))//Đặt giá trị valid của phản hồi dựa trên việc JWT có được xác minh và thời gian hết hạn còn hiệu lực hay không
                .build();//Xây dựng đối tượng IntrospectResponse và trả về.

    }

    @NonFinal // đánh dấu để lombok không inject dependency vào construct
    @Value("${jwt.signerKey}")//springframework.annotation.Value; // dùng để injection 1 property ở application vào variable
    protected String SIGNER_KEY ;//cipher key-khóa bí mật -generate random

    public AuthenticationResponse Authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUserName(request.getUserName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        //xác thực mật khẩu có khớp với mật khẩu đã lưu
        boolean authenticated= passwordEncoder.matches(request.getPassWord(), user.getPassWord());
        if (!authenticated){
            throw  new AppException(ErrorCode.AUTHENTICATION_NOT_SUCCESS);
        }else{
            var token = generateToken(request.getUserName());

            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .build();
        }

    }


     String generateToken(String userName){
        //dependency nimbus Token gồm [header;payload;signature(header,payload)]
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512); // thuat toan SHA512

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userName)      //username
                .issuer("com.vn.sbit") //domain
                .issueTime(new Date())  //thoi gian tao
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))//thoi gian token het han
                .claim("customClaim","Custom")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader,payload);//header - payload

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); // ký private key signer_key
            return jwsObject.serialize();//trả về objectjwt đc ký thành dạng chuỗi bằng serialize(sẽ được đầy đủ)-gồm header-paypal-signature
        } catch (JOSEException e) {
            log.error("Cannot create token "+e);
            throw new RuntimeException(e);
        }

    }
}
