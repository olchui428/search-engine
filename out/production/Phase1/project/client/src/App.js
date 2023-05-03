import logo from './logo.svg';
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
  Spin
} from 'antd';
import { LoadingOutlined } from '@ant-design/icons';
import {
  createBrowserRouter,
  RouterProvider,
  Route,
  Link
} from 'react-router-dom';

import _ from 'lodash';
import Crawler from './Crawler';
import SearchEngine from './SearchEngine';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Crawler />
  },
  {
    path: '/search',
    element: <SearchEngine />
  }
]);

const App = () => {
  return <RouterProvider router={router} />;
};

export default App;
