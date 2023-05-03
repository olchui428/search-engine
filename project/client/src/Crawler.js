import axios from 'axios';
import React, { useState } from 'react';
import { Layout, Space, Input, Button, Spin } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const Crawler = () => {
  const { Search } = Input;
  const navigate = useNavigate();

  const CRAWL_API = 'http://localhost:8000/crawl';

  const [crawlLoading, setCrawlLoading] = useState(false);

  const antIcon = <LoadingOutlined style={{ fontSize: 24 }} spin />;

  const onCrawl = async (value) => {
    setCrawlLoading(true);
    console.log(value);
    const fetchData = await axios.get(CRAWL_API, {
      params: { startingURL: 'https://cse.hkust.edu.hk/', numPages: 30 }
    });
    console.log(fetchData.data);
    setCrawlLoading(false);
    navigate('/search');
  };

  return (
    <Layout
      style={{
        display: 'flex',
        minHeight: '100vh',
        padding: '5rem',
        gap: '1rem',
        justifyContent: 'center'
      }}>
      {!crawlLoading && (
        <Search
          placeholder="Which URL do you want to start crawling?"
          onSearch={onCrawl}
          enterButton={<Button type="primary">Crawl</Button>}
          size="large"
        />
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
