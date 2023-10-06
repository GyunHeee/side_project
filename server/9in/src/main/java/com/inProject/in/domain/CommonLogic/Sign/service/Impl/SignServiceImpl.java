package com.inProject.in.domain.CommonLogic.Sign.service.Impl;

import com.inProject.in.Global.CommonResponse;
import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.CommonLogic.Mail.service.MailService;
import com.inProject.in.domain.CommonLogic.RefreshToken.entity.RefreshToken;
import com.inProject.in.domain.CommonLogic.RefreshToken.repository.RefreshTokenRepository1;
import com.inProject.in.domain.CommonLogic.Sign.Dto.request.*;
import com.inProject.in.domain.CommonLogic.Sign.Dto.response.*;
import com.inProject.in.domain.CommonLogic.Sign.service.SignService;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SignServiceImpl implements SignService {

    private final Logger log = LoggerFactory.getLogger(SignServiceImpl.class);

    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenRepository1 refreshTokenRepository;
    private final MailService mailService;
    private RedisTemplate redisTemplate;

    @Autowired
    public SignServiceImpl(UserRepository userRepository,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder,
                           RefreshTokenRepository1 refreshTokenRepository,
                           MailService mailService,
                           RedisTemplate redisTemplate){

        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public ResponseSignUpDto signUp(RequestSignUpDto requestSignUpDto) {
        log.info("SignService signup ==> 회원가입 정보 확인");
        User user;
        //String userId = requestSignUpDto.getUserId();
        String username = requestSignUpDto.getUsername();
        String password = requestSignUpDto.getPassword();
        String mail = requestSignUpDto.getMail();
        String role = requestSignUpDto.getRole();

        if(userRepository.getByUsername(username).isPresent()){   //아이디 중복 확인
            throw new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.CONFLICT, "아이디 중복");
        }

        if(role.equalsIgnoreCase("admin")){
            user = User.builder()
                    //.userId(userId)
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .mail(mail)
                    .roles(Collections.singletonList("ROLE_ADMIN"))
                    .build();
        }
        else{
            user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .mail(mail)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
        }
        // 여기서 오류 뜨는데 Error: 1364-HY000: Field 'user_id' doesn't have a default value
        User savedUser = userRepository.save(user);
        ResponseSignUpDto responseSignUpDto = new ResponseSignInDto();

        log.info("SignService signup ==> user 저장");
        if(!savedUser.getUsername().isEmpty()){
           log.info("signup 정상 동작 완료");
           setSuccess(responseSignUpDto);
        }
        else{
            log.info("signup 실패");
            setFailed(responseSignUpDto);
        }

        return responseSignUpDto;
    }

    @Override
    @Transactional
    public ResponseSignInDto signIn(RequestSignInDto requestSignInDto) {
        log.info("SignService signin ==> 회원 인증 확인 시작");
        String username = requestSignInDto.getUsername();
        String password = requestSignInDto.getPassword();

        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "잘못된 id"));

        log.info("회원 id : " + username);

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "비밀번호 불일치");
        }

        log.info("비밀번호 일치");

        String accessToken = jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

       // Optional<RefreshToken> findRefreshToken = refreshTokenRepository.getByUsername(username);

//        if(!findRefreshToken.isPresent()){      //refresh토큰이 없다면 새로 생성해서 DB에 insert, 있다면 갱신.
//            RefreshToken newRefreshToken = RefreshToken.builder()
//                    .user(user)
//                    .refreshToken(refreshToken)
//                    .build();
//
//            RefreshToken savedRefreshToken = refreshTokenRepository.save(newRefreshToken);
//        }
//        else{
//            findRefreshToken.get().updateRefreshToken(refreshToken);
//
//            RefreshToken savedRefreshToken = refreshTokenRepository.save(findRefreshToken.get());
//        }

        refreshTokenRepository.findByUsername(username)         //위의 if문은 이렇게 간단하게도 만들 수 있다.
                .ifPresentOrElse(
                        token -> {
                            token.updateRefreshToken(refreshToken);
                            log.info("updated refresh token : " + token.toString());
                            },
                        () -> {
                            refreshTokenRepository.save(new RefreshToken(username, refreshToken));
                            log.info("new refresh token " + username);
                        }
                        );

        ResponseSignInDto responseSignInDto = ResponseSignInDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user_id(user.getId())
                .username(username)
                .build();


        log.info("ResponseSignInDto 생성");
        setSuccess(responseSignInDto);
        return responseSignInDto;
    }

    @Override
    public ResponseRefreshDto reissue(RequestRefreshDto requestRefreshDto, HttpServletRequest request) {

        log.info("reissue ==> refresh 토큰 통한 토큰 재발급 시작");
        log.info("get refreshtoken : " + requestRefreshDto.getRefreshToken());
        String refreshToken = requestRefreshDto.getRefreshToken();


        if(!jwtTokenProvider.validateRefreshToken(refreshToken)){   //refresh 토큰이 유효기간이 지났는지 검증
            throw new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "refresh 토큰이 유효하지 않음.");
        }
        log.info("reissue ==> refresh 토큰 검증 성공");

        String username = jwtTokenProvider.getUsername(refreshToken);

        RefreshToken findRefreshToken = refreshTokenRepository.findByUsername(username)    //DB에 실제로 그 유저에게 발급된 refresh토큰이 있는지 확인
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "refresh토큰 만료. 로그아웃된 사용자"));
        log.info("reissue ==> DB에 사용자 이름과 refresh 토큰 존재 확인");


        if(!findRefreshToken.getRefreshToken().equals(refreshToken)){
            throw new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "DB의 refresh토큰과 일치하지 않음.");
        }
        log.info("reissue ==> DB refresh token과 일치 확인");

        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, username + " 은 없는 사용자 아이디입니다."));


        String newAccessToken = jwtTokenProvider.createToken(     //새로운 토큰 발급.
                username,
                user.getAuthorities()
                        .stream()
                        .map(Objects::toString)
                        .collect(Collectors.toList())
        );

        log.info("reissue ==> 새 토큰 발급 : " + newAccessToken);
        log.info("토큰 authorities : " +  user.getAuthorities()
                .stream()
                .map(Objects::toString)
                .collect(Collectors.toList()));        //잘 되는 지 확인용. 나중엔 지워야 함.

        String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

        findRefreshToken.updateRefreshToken(newRefreshToken);    //refresh 토큰도 업데이트.
        refreshTokenRepository.save(findRefreshToken);

        ResponseRefreshDto responseRefreshDto = new ResponseRefreshDto(newAccessToken, newRefreshToken);

        return responseRefreshDto;
    }

    public void logout(RequestLogoutDto requestLogoutDto){
        String accessToken = requestLogoutDto.getAccessToken();
        String refreshToken = requestLogoutDto.getRefreshToken();

        if(!jwtTokenProvider.validateToken(accessToken)){   //access 토큰이 유효한 지 검사한다.
            throw new CustomException(ConstantsClass.ExceptionClass.SIGN, HttpStatus.BAD_REQUEST, "로그아웃에서 유효하지 않은 access 토큰");
        }
        log.info("logout 전 access 토큰 유효성 검사 완료");

        String username = jwtTokenProvider.getUsername(accessToken);

        if(!refreshTokenRepository.findByUsername(username).isEmpty()){  //redis 에 있는 유저의 refresh토큰을 삭제한다.
            refreshTokenRepository.delete(username);
        }

        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue().set(accessToken, "blackListed", expiration, TimeUnit.MILLISECONDS);  //블랙리스트에 access토큰등록. 토큰이 만료될때까지, redis에 등록됨.
    }

    private void setSuccess(ResponseSignUpDto responseSignUpDto){
        responseSignUpDto.setSuccess(true);
        responseSignUpDto.setCode(CommonResponse.SUCCESS.getCode());
        responseSignUpDto.setMsg(CommonResponse.SUCCESS.getMsg());
    }
    private void setFailed(ResponseSignUpDto responseSignUpDto){
        responseSignUpDto.setSuccess(false);
        responseSignUpDto.setCode(CommonResponse.FAIL.getCode());
        responseSignUpDto.setMsg(CommonResponse.FAIL.getMsg());
    }
}
