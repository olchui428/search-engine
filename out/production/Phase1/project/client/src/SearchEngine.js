import axios from 'axios';
import React, { useState } from 'react';
import {
  Layout,
  Space,
  Input,
  Button,
  Card,
  Avatar,
  Badge,
  Switch,
  Spin,
  Typography,
  InputNumber
} from 'antd';
import {
  LoadingOutlined,
  PlusOutlined,
  MinusOutlined
} from '@ant-design/icons';

import _ from 'lodash';

const SearchEngine = () => {
  const QUERY_API = 'http://localhost:8000/query';
  const { Search } = Input;
  const { Text } = Typography;

  const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />;

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState([]);
  const [numResult, setNumResult] = useState(10);

  const onSearch = async (value) => {
    setLoading(true);
    console.log(value);
    const fetchData = await axios.get(`${QUERY_API}?query=${value}`, {
      params: { numPages: numResult }
    });
    console.log(fetchData.data);
    setResult(fetchData.data);
    setLoading(false);
  };

  const onChangeNumber = (value) => {
    console.log('changed', value);
    setNumResult(value);
  };

  const formatDate = (timestamp) => {
    const d = new Date(timestamp * 1000);
    let ye = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(d);
    let mo = new Intl.DateTimeFormat('en', { month: '2-digit' }).format(d);
    let da = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(d);
    return `${da}-${mo}-${ye}`;
  };

  return (
    <Layout
      style={{
        display: 'flex',
        minHeight: '100vh',
        padding: '5rem',
        gap: '1rem'
      }}>
      {/* Search Engine */}
      <div style={{ gap: '1rem', display: 'flex' }}>
        <Search
          // style={{ width: '75%' }}
          placeholder="What are you looking for?"
          onSearch={onSearch}
          enterButton={
            loading && (
              <Button style={{ height: '100%' }}>
                <Spin indicator={antIcon} spinning={loading} />
              </Button>
            )
          }
          size="large"
        />
        {/* <div> */}
        <Text disabled style={{ textAlign: 'right' }}>
          Number of Results
        </Text>
        <InputNumber
          min={0}
          defaultValue={numResult}
          onChange={onChangeNumber}
        />
        {/* </div> */}

        {/* <Button.Group style={{ width: '25%', margin: 3 }}>
          <Button>
            <MinusOutlined />
          </Button>
          <Button>
            <MinusOutlined />
          </Button>
          <Button>
            <PlusOutlined />
          </Button>
        </Button.Group> */}
      </div>

      {/* Search Results */}
      <Layout style={{ gap: '1rem' }}>
        {_.map(result, (item) => (
          <Card
            title={
              <a target="_blank" href={item.url} style={{ color: 'black' }}>
                {item.title}
              </a>
            }
            style={{ justifyContent: 'start' }}
            bordered={false}>
            <Card.Grid
              style={{ textAlign: 'left', width: '100%' }}
              hoverable={false}>
              <a target="_blank" href={item.url}>
                {item.url}
              </a>
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Score
            </Card.Grid>
            <Card.Grid
              style={{ width: '75%', textAlign: 'left' }}
              hoverable={false}>
              {item.score}
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Last modified at
            </Card.Grid>
            <Card.Grid
              style={{ width: '75%', textAlign: 'left' }}
              hoverable={false}>
              {formatDate(item.modifiedAt)}
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Size
            </Card.Grid>
            <Card.Grid
              style={{ width: '75%', textAlign: 'left' }}
              hoverable={false}>
              {item.size}
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Keywords
            </Card.Grid>
            <Card.Grid
              style={{
                width: '75%',
                padding: 1
              }}
              hoverable={false}>
              <div
                style={{
                  height: 100,
                  padding: '1rem',
                  textAlign: 'left',
                  overflowY: 'scroll'
                }}>
                {_.map(item.forwardIndex, (word) => (
                  <Button.Group style={{ margin: 3 }}>
                    <Button className="adjust_button_hover_drawer">
                      {word.word}
                    </Button>
                    <Button className="adjust_button_hover_drawer">
                      {word.tf}
                    </Button>
                  </Button.Group>
                ))}
              </div>
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Parent links
            </Card.Grid>
            <Card.Grid
              style={{
                width: '75%',
                padding: 1
              }}
              hoverable={false}>
              <div
                style={{
                  height: 100,
                  padding: '1rem',
                  textAlign: 'left',
                  overflowY: 'scroll'
                }}>
                {_.map(item.parentLinks, (link) => (
                  <p>
                    <a target="_blank" href={link}>
                      {link}
                    </a>
                  </p>
                ))}
              </div>
            </Card.Grid>
            <Card.Grid
              style={{ width: '25%', textAlign: 'left' }}
              hoverable={false}>
              Child links
            </Card.Grid>
            <Card.Grid
              style={{
                width: '75%',
                padding: 1
              }}
              hoverable={false}>
              <div
                style={{
                  height: 100,
                  padding: '1rem',
                  textAlign: 'left',
                  overflowY: 'scroll'
                }}>
                {_.map(item.childLinks, (link) => (
                  <p>
                    <a target="_blank" href={link}>
                      {link}
                    </a>
                  </p>
                ))}
              </div>
            </Card.Grid>
          </Card>
        ))}
      </Layout>
    </Layout>
  );
};

export default SearchEngine;
