import React from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import MypageUser from '../../../ components/Mypage/MypageUser';
import MypageSchool from '../../../ components/Mypage/MypageSchool';
import MypageEtc from '../../../ components/Mypage/MypageEtc';
import MypageLicese from '../../../ components/Mypage/MypageLicese';
import MypageJob from '../../../ components/Mypage/MypageJob';
import MypageProject from '../../../ components/Mypage/MypageProject';

export default function MyPage() {
  const navigate = useNavigate();

  const handleRecruit = () => {
    navigate('/mypage/recruit');
  };

  const handleClip = () => {
    navigate('/mypage/clip');
  };

  return (
    <Mypage>
      <Section1>
        <MypageUser />
        <MypageSchool />
        <MypageEtc />
      </Section1>
        <MypageLicese />
        <MypageJob />
        <MypageProject />
    </Mypage>
  );
}

const Mypage = styled.div`
  width: 1300px;
  padding-top: 50px;
  margin: auto;
  height: 2000px;
`;

const Section1 = styled.div`
  display: flex;
  justify-content: center;
  gap: 20px;
  .section1_profile {
    position: relative;
    width: 660px;
    height: 416px;
    flex-shrink: 0;
    border-radius: 50px;
    border: 2px solid #dae9fc;
    background: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .section1_etc {
    width: 200px;
    height: 318px;
    flex-shrink: 0;
    border-radius: 50px;
    border: 2px solid #dae9fc;
    background: #fff;
  }

  .section1_fix_btn {
    display: flex;
    width: 100px;
    bottom: 20px;
    position: absolute;
    height: 34px;
    padding: 10px;
    justify-content: center;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    border-radius: 5px;
    border: 1px solid #d2e2ec;
    background: #fff;
    box-shadow: 0px 0px 0px 0px #cbdafc;
    cursor: pointer;
    color: #1f7ceb;
  }

  .section_profile_flex {
    div {
      display: flex;
      gap: 5px;
      margin-bottom: 5px;
      align-items: center;
      span {
        display: inline-block;
        width: 120px;
        text-align: center;
      }

      input {
        display: flex;
        width: 187px;
        height: 22px;
        padding: 10px;
        align-items: center;
        gap: 5px;
        flex-shrink: 0;
        border-radius: 5px;
        border: 1px solid #d2e2ec;
        background: var(--bs-white, #fff);
        box-shadow: 0px 0px 0px 0px #cbdafc;
      }

      select {
        width: 208px;
        height: 42px;
        border-radius: 5px;
        border: 1px solid #d2e2ec;
        background: var(--bs-white, #fff);
        box-shadow: 0px 0px 0px 0px #cbdafc;
      }

      .skill_stack {
      }

      .add_btn {
        display: flex;
        width: 74px;
        height: 34px;
        padding: 10px;
        justify-content: center;
        align-items: center;
        gap: 10px;
        flex-shrink: 0;
        color: #1f7ceb;
        font-family: SUITE Variable;
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
        border-radius: 5px;
        border: 1px solid #d2e2ec;
        background: #fff;
        box-shadow: 0px 0px 0px 0px #cbdafc;
        cursor: pointer;
      }
    }
  }
`;


