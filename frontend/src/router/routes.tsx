import { createBrowserRouter } from 'react-router-dom';
import App from '../App.tsx';
import { RequireAuth } from '../components/RequireAuth.tsx';
import { LoginPage } from '../pages/LoginPage.tsx';
import { HomePage } from '../pages/HomePage.tsx';
import { VehiclesPage } from '../pages/VehiclesPage.tsx';
import { NewVehiclePage } from '../pages/NewVehiclePage.tsx';
import { EditVehiclePage } from '../pages/EditVehiclePage.tsx';
import { DealersPage } from '../pages/DealersPage.tsx';
import { NewDealerPage } from '../pages/NewDealerPage.tsx';
import { EditDealerPage } from '../pages/EditDealerPage.tsx';
import { DealerVehiclesPage } from '../pages/DealerVehiclesPage.tsx';
import { SobrePage } from '../pages/SobrePage.tsx';
import { NotFoundPage } from '../pages/NotFoundPage.tsx';

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: (
      <RequireAuth>
        <App />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <HomePage /> },
      { path: 'veiculos', element: <VehiclesPage /> },
      { path: 'veiculos/novo', element: <NewVehiclePage /> },
      { path: 'veiculos/:id/editar', element: <EditVehiclePage /> },
      { path: 'concessionarias', element: <DealersPage /> },
      { path: 'concessionarias/novo', element: <NewDealerPage /> },
      { path: 'concessionarias/:id/editar', element: <EditDealerPage /> },
      { path: 'concessionarias/:id/veiculos', element: <DealerVehiclesPage /> },
      { path: 'sobre', element: <SobrePage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
