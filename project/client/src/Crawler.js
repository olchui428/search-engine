import axios from 'axios';
import React, { useState } from 'react';
import {
  Layout,
  Space,
  Input,
  Button,
  Spin,
  Row,
  Col,
  Typography,
  InputNumber
} from 'antd';
import { LoadingOutlined, RightCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const Crawler = () => {
  const { Search } = Input;
  const { Text } = Typography;
  const navigate = useNavigate();

  const CRAWL_API = 'http://localhost:8000/crawl';

  const [crawlLoading, setCrawlLoading] = useState(false);
  const [numResult, setNumResult] = useState(10);

  const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />;

  const onCrawl = async (value) => {
    setCrawlLoading(true);
    console.log(value);
    const fetchData = await axios.get(CRAWL_API, {
      params: { startingURL: value, numPages: numResult }
    });
    console.log(fetchData.data);
    setCrawlLoading(false);
    navigate('/search');
  };

  const onChangeNumber = (value) => {
    console.log('changed', value);
    setNumResult(value);
  };

  return (
    <Layout
      style={{
        display: 'flex',
        minHeight: '100vh',
        paddingLeft: '30%',
        paddingRight: '30%',
        paddingTop: '10rem',
        gap: '1rem'
        // justifyContent: 'center'
      }}>
      {!crawlLoading && (
        <>
          <div style={{ gap: '1rem', display: 'flex', marginBottom: '1rem' }}>
            <Search
              placeholder="Which URL do you want to start crawling?"
              onSearch={onCrawl}
              enterButton={<Button type="primary">Crawl</Button>}
              size="large"
              // style={{ marginBottom: '1rem' }}
            />
            <Text disabled style={{ textAlign: 'center' }}>
              Number of pages to crawl
            </Text>
            <InputNumber
              min={0}
              defaultValue={numResult}
              onChange={onChangeNumber}
            />
          </div>
          <Button type="primary" size="large" href="/search">
            Search from previously crawled pages
            <RightCircleOutlined style={{ marginLeft: '1rem' }} />
          </Button>
        </>
      )}
      {crawlLoading && (
        <>
          <Spin indicator={antIcon} spinning={crawlLoading} />
          <p>This may take some time...</p>
        </>
      )}
    </Layout>
  );
};

export default Crawler;
