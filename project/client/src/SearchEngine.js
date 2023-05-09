import axios from 'axios';
import React, { useState, useEffect } from 'react';
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
  InputNumber,
  List,
  Skeleton
} from 'antd';
import {
  LoadingOutlined,
  PlusOutlined,
  MinusOutlined,
  LeftCircleOutlined
} from '@ant-design/icons';

import _ from 'lodash';

const STEP = 2;

const SearchEngine = () => {
  const QUERY_API = 'http://localhost:8000/query';
  const { Search } = Input;
  const { Text } = Typography;

  const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />;

  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [result, setResult] = useState([]);
  const [numResult, setNumResult] = useState(10);
  const [numResultShow, setNumResultShow] = useState(STEP);
  const [clickedSearch, setClickedSearch] = useState(false);

  const onSearch = async (value) => {
    setNumResultShow(STEP);
    setClickedSearch(true);
    setLoading(true);
    console.log(value);
    const fetchData = await axios.get(`${QUERY_API}?query=${value}`, {
      params: { numPages: numResult }
    });
    setData(fetchData.data);
    setResult(fetchData.data.slice(0, STEP));
    setLoading(false);
  };

  const onLoadMore = () => {
    setNumResultShow(numResultShow + STEP);
    setLoading(true);
    setResult(data.slice(0, numResultShow));
    setLoading(false);
  };

  const onClickKeyword = async (word) => {
    window.scrollTo(0, 0);
    setKeyword(word);
    setLoading(true);
    const fetchData = await axios.get(`${QUERY_API}?query=${word}`, {
      params: { numPages: numResult }
    });
    console.log(fetchData.data);
    setResult(fetchData.data);
    setLoading(false);
  };

  const onChangeKeyword = (value) => {
    // console.log('onChangeKeyword', value);
    setKeyword(value);
  };

  const onChangeNumber = (value) => {
    // console.log('changed', value);
    setNumResult(value);
  };

  // useEffect(() => {
  //   console.log('keyword>>>', keyword);
  // }, [keyword]);

  const formatDate = (timestamp) => {
    const d = new Date(timestamp * 1000);
    let ye = new Intl.DateTimeFormat('en', { year: 'numeric' }).format(d);
    let mo = new Intl.DateTimeFormat('en', { month: '2-digit' }).format(d);
    let da = new Intl.DateTimeFormat('en', { day: '2-digit' }).format(d);
    return `${da}-${mo}-${ye}`;
  };

  const loadMore = !loading ? (
    <div
      style={{
        textAlign: 'center',
        marginTop: 12,
        height: 32,
        lineHeight: '32px'
      }}>
      <Button onClick={onLoadMore}>Load more</Button>
    </div>
  ) : null;

  const PageCard = ({ item }) => {
    return (
      <Card
        title={
          <a target="_blank" href={item.url} style={{ color: 'black' }}>
            {item.title}
          </a>
        }
        style={{ justifyContent: 'start', marginBottom: '1rem' }}
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
                <Button onClick={() => onClickKeyword(word.word)}>
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
    );
  };

  return (
    <Layout
      style={{
        display: 'flex',
        minHeight: '100vh',
        paddingLeft: '10%',
        paddingRight: '10%',
        paddingTop: '5rem',
        paddingBottom: '5rem',
        gap: '1rem'
      }}>
      <Button
        icon={<LeftCircleOutlined />}
        style={{ width: '20%' }}
        type="primary"
        size="large"
        href="/">
        Back to Crawling
      </Button>

      {/* Search Engine */}
      <div style={{ gap: '1rem', display: 'flex' }}>
        <Search
          // style={{ width: '75%' }}
          placeholder="What are you looking for?"
          onChange={(e) => onChangeKeyword(e.target.value)}
          onSearch={onSearch}
          value={keyword}
          enterButton={
            loading && (
              <Button style={{ height: '100%' }}>
                <Spin indicator={antIcon} spinning={loading} />
              </Button>
            )
          }
          size="large"
        />

        <Text disabled style={{ textAlign: 'right' }}>
          Number of Results
        </Text>
        <InputNumber
          min={0}
          defaultValue={numResult}
          onChange={onChangeNumber}
        />
      </div>

      <List
        className="demo-loadmore-list"
        // loading={initLoading}
        itemLayout="horizontal"
        loadMore={loadMore}
        dataSource={result}
        renderItem={(item) => <PageCard item={item} />}
      />

      {/* Search Results */}
    </Layout>
  );
};

export default SearchEngine;
