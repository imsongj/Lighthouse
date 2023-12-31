import React, { useEffect, useState } from 'react'
import { Select, Modal, Button, Tooltip } from 'antd'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { userAction } from '../store/user'

export default function UserPage() {
  const dispatch = useDispatch()
  const location = useLocation()
  const navigate = useNavigate()
  const [isModalVisible, setIsModalVisible] = useState(false)
  const profile = useSelector(state => state.user.profile)
  useEffect(() => {
    const { userId } = location.state
    dispatch(userAction.profile(userId))
    dispatch(userAction.getFollowing())
  }, [location.state.userId])

  const { userId } = location.state
  const myId = Number(sessionStorage.getItem('userId'))

  const following = useSelector(state => state.user.following)

  const showModal = () => {
    setIsModalVisible(true)
  }

  const handleOk = () => {
    setIsModalVisible(false)
  }

  const handleCancel = () => {
    setIsModalVisible(false)
  }

  const handleMoveTempDetailPage = val => {
    navigate(`/temp/${val}`)
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-around',
        marginTop: '-47px',
      }}
    >
      {userId !== myId && (
        <div>
          {/* 버튼 렌더링 */}
          {!following?.find(id => id === profile.id) ? (
            <Button
              type="primary"
              onClick={() => {
                dispatch(userAction.follow(profile.id)).then(() => {
                  dispatch(userAction.getFollowing())
                  dispatch(userAction.profile(profile.id))
                })
                // window.location.reload()
              }}
            >
              팔로우
            </Button>
          ) : (
            <Button
              type="default"
              onClick={() => {
                dispatch(userAction.unfollow(profile.id)).then(() => {
                  dispatch(userAction.getFollowing())
                  dispatch(userAction.profile(profile.id))
                })
              }}
            >
              언팔로우
            </Button>
          )}
        </div>
      )}
      <Link
        to="/user_edit/me"
        state={{ userId: Number(sessionStorage.getItem('userId')) }}
      >
        마이페이지
      </Link>
      <div
        className="comp"
        style={{
          width: '60%',
          height: '100%',
          border: '2px solid #999999',
          borderRadius: '20px',
          textAlign: 'center',
          backgroundColor: 'white',
          // display: 'flex',
          justifyContent: 'center',
          alignContent: 'center',
          boxShadow: '4px 4px 15px rgba(0, 0, 0, 0.4)',
          position: 'relative',
          margin: '20px',
          padding: '20px',
        }}
      >
        <div className="circular-image">
          <img src="/logo192.png" alt="안뜸" />
        </div>
        <div className="grid-container">
          <div className="item">
            <div
              style={{
                position: 'absolute',
                border: '1px solid #177AEE',
                backgroundColor: '#177AEE',
                color: 'white',
                borderRadius: '20px',
                padding: '8px',
                flexDirection: 'flex-start',
                marginTop: '20px',
              }}
            >
              <div style={{ textAlign: 'left', display: 'flex' }}>
                {profile.tags?.map(tag => (
                  <p key={tag.id}>&nbsp;#{tag.keyword} &nbsp;</p>
                ))}
              </div>
            </div>
          </div>
          <div className="item">
            <Button
              type="primary"
              onClick={showModal}
              style={{
                width: '200px',
                border: '1px solid #3E5D99',
                backgroundColor: '#3E5D99',
                color: 'white',
                borderRadius: '20px',
                padding: '8px',
                fontWeight: 'bold',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                marginTop: '20px',
                marginLeft: '200px',
              }}
            >
              사용중인 템플릿 보러가기
            </Button>

            <Modal
              title="사용중인 템플릿"
              visible={isModalVisible}
              onOk={handleOk}
              onCancel={handleCancel}
            >
              {/* 모달 내용 */}
              <p>여기에 템플릿에 관한 상세한 내용을 넣을 수 있습니다.</p>
              <p>
                더 많은 정보와 버튼 등을 추가하여 원하는 대화 상자를 만들 수
                있습니다.
              </p>
            </Modal>
          </div>
          <div className="item">
            <div style={{ position: 'absolute', top: '10px', right: '30px' }}>
              <Tooltip title="팔로워 목록 보기" placement="bottom">
                <div>팔로워 {profile.follower}</div>
              </Tooltip>
              <Tooltip title="팔로잉 목록 보기" placement="bottom">
                <div>팔로잉 {profile.following}</div>
              </Tooltip>
            </div>
          </div>
        </div>
        <div className="container1">
          <div className="u_item">닉네임</div>
          <div className="u_item1">{profile.nickname}</div>
          <div className="u_item">별점</div>
          <div className="u_item1">{profile.score}</div>
          <div className="u_item">자기소개</div>
          <div className="u_item1">{profile.description}</div>

          <div className="u_item">뱃지 목록</div>
          <div className="u_item1" />

          <div className="u_item">신청 중</div>
          <div>
            <Select
              className="u_item2"
              value="신청중인 스터디"
              onChange={handleMoveTempDetailPage}
            >
              {profile.participatedStudies
                ?.filter(study => study.leaderProfile.id !== myId)
                .map(study => (
                  <Select.Option value={study.id} key={study.id}>
                    <Link to={`/study/${study.id}`} state={{ id: study.id }}>
                      {study.title}
                    </Link>
                  </Select.Option>
                ))}
            </Select>
          </div>

          <div className="u_item">편집 중</div>
          <div>
            <Select
              className="u_item2"
              value="편집중인 스터디"
              onChange={handleMoveTempDetailPage}
            >
              {profile.participatedStudies
                ?.filter(study => study.leaderProfile.id === myId)
                .map(study => (
                  <Select.Option value={study.id} key={study.id}>
                    {/* <Link to={`/temp/${study.id}`} state={{ id: study.id }}> */}
                    {study.title}
                    {/* </Link> */}
                  </Select.Option>
                ))}
            </Select>
          </div>

          <div className="u_item">진행 예정</div>
          <div>
            <Select className="u_item2" value="진행 예정인 스터디">
              {profile.recruitingStudies?.map(study => (
                <Select.Option value={study.title} key={study.id}>
                  <Link to={`/study/${study.id}`} state={{ id: study.id }}>
                    {study.title}
                  </Link>
                </Select.Option>
              ))}
            </Select>
          </div>
          <div className="u_item">진행 중</div>
          <div>
            <Select className="u_item2" value="진행중인 스터디">
              {profile.progressStudies?.map(study => (
                <Select.Option value={study.title} key={study.id}>
                  <Link to={`/study/${study.id}`} state={{ id: study.id }}>
                    {study.title}
                  </Link>
                </Select.Option>
              ))}
            </Select>
          </div>

          <div className="u_item">참여했던 스터디</div>
          <div>
            <Select className="u_item2" value="참여했던 스터디">
              {profile.terminatedStudies?.map(study => (
                <Select.Option value={study.title} key={study.id}>
                  <Link to={`/study/${study.id}`} state={{ id: study.id }}>
                    {study.title}
                  </Link>
                </Select.Option>
              ))}
            </Select>
          </div>

          <div className="u_item">북마크</div>
          <div>
            <Select className="u_item2" value="북마크한 스터디">
              {profile.bookmarkStudies?.map(study => (
                <Select.Option value={study.title} key={study.id}>
                  <Link to={`/study/${study.id}`} state={{ id: study.id }}>
                    {study.title}
                  </Link>
                </Select.Option>
              ))}
            </Select>
          </div>
        </div>
      </div>
    </div>
  )
}
