package com.inProject.in.domain.User.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inProject.in.Global.BaseEntity;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Comment.entity.Comment;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.entity.ClipBoardRelation;
import com.inProject.in.domain.Profile.entity.Certificate;
import com.inProject.in.domain.Profile.entity.Education;
import com.inProject.in.domain.Profile.entity.Job_ex;
import com.inProject.in.domain.Profile.entity.Project_skill;
import com.inProject.in.domain.User.Dto.UpdateUserDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Table(name = "user") //테이블과 매핑
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class User extends BaseEntity implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

//    @Column(nullable = false)
//    private String userId;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String mail;
    @Column
    private boolean deleted = Boolean.FALSE;


    //연관관계

    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Board> authoredBoardList;   //작성한 글들

    @OneToMany(mappedBy = "board_applicant", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<ApplicantBoardRelation> applicantBoardRelationList; //지원한 게시글들

    @OneToMany(mappedBy = "role_applicant", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<ApplicantRoleRelation> applicantRoleRelationList;  //지원한 역할

    @OneToMany(mappedBy = "clipUser", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<ClipBoardRelation> clipBoardRelationList; //관심 클립으로 지정한 게시글들

    @OneToMany(mappedBy = "user",  cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Comment> commentList;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Job_ex jobEx;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Education education;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Project_skill projectSkill;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Certificate certificate;




    //권한 인증

    @ElementCollection(fetch = FetchType.EAGER)  //지연로딩을 통해 실제로 이 컬렉션을 사용할 때만 접근하도록 함
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String getUsername(){
        return this.username;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {                 //계정이 만료되었는지를 나타냄. true는 만료되지 않음을 뜻함
        return true;
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {                  //계정이 잠겨잇는지를 나타냄, true는 잠기지 않음을 뜻함
        return true;
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {             //비밀번호가 만드료되었는지를 나타냄, true는 만료되지 않음을 뜻함
        return true;
    }
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {                           //계정이 활성화되었는지 나타냄, true는 활성화 상태
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void updateUser(UpdateUserDto updateUserDto){
        this.username = updateUserDto.getUsername();
        this.password = updateUserDto.getPassword();
        this.mail = updateUserDto.getMail();

    }

}
