import React from 'react'
import { Input, Select, Space } from 'antd'
import { useDispatch } from 'react-redux'
import { setText } from '../../store/study'

// 검색창
const { Search } = Input

function SearchComponent() {
  const dispatch = useDispatch()
  const onSearch = value => {
    dispatch(setText(value))
  }
  // 지역
  const handleChange = value => {
    console.log(`selected ${value}`)
  }
  return (
    <div
      style={{
        margin: '10px',
        marginLeft: '230px',
      }}
    >
      <h2>현재 모집 중인 스터디</h2>
      <div style={{ display: 'flex', width: '100%' }}>
        {/* 검색창 */}
        <div>
          <Search
            placeholder="input search text"
            onSearch={onSearch}
            enterButton
            style={{ width: '500px', margin: '10px' }}
          />
        </div>
        {/* 지역 */}
        <div>
          <Space wrap>
            <Select
              defaultValue="lucy"
              style={{
                width: 120,
                margin: '10px',
              }}
              onChange={handleChange}
              options={[
                {
                  value: 'jack',
                  label: 'Jack',
                },
                {
                  value: 'lucy',
                  label: 'Lucy',
                },
                {
                  value: 'Yiminghe',
                  label: 'yiminghe',
                },
                {
                  value: 'disabled',
                  label: 'Disabled',
                  disabled: true,
                },
              ]}
            />
            <Select
              defaultValue="lucy"
              style={{
                width: 120,
                margin: '10px',
              }}
              onChange={handleChange}
              options={[
                {
                  value: 'jack',
                  label: 'Jack',
                },
                {
                  value: 'lucy',
                  label: 'Lucy',
                },
                {
                  value: 'Yiminghe',
                  label: 'yiminghe',
                },
                {
                  value: 'disabled',
                  label: 'Disabled',
                  disabled: true,
                },
              ]}
            />
          </Space>
        </div>
      </div>
    </div>
  )
}

export default SearchComponent