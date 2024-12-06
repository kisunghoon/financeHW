package zerobase.dayone.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.dayone.model.Auth;
import zerobase.dayone.security.TokenProvider;
import zerobase.dayone.service.MemberService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Auth.Singup request){
        //회원 가입
        var result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> singIn(@RequestBody Auth.SignIn request){

        var member = this.memberService.authenticate(request);
        var token = this.tokenProvider.generateToken(member.getUsername(),member.getRoles());
        log.info("user login -> "+request.getUsername());
        return ResponseEntity.ok(token);
    }
}
