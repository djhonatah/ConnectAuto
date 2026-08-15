import { createBrowserRouter } from 'react-router-dom';
import App from '../App.tsx';
import { HomePage } from '../pages/HomePage.tsx';
import { VehiclesPage } from '../pages/VehiclesPage.tsx';
import { SobrePage } from '../pages/SobrePage.tsx';
import { NotFoundPage } from '../pages/NotFoundPage.tsx';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'veiculos', element: <VehiclesPage /> },
      { path: 'sobre', element: <SobrePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
