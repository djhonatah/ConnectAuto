import { createBrowserRouter } from 'react-router-dom';
import App from '../App.tsx';
import { HomePage, SobrePage, NotFoundPage } from './placeholders.tsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'sobre', element: <SobrePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
