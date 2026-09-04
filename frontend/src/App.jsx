import { Route, Routes } from 'react-router-dom';

import OrderBoard from './components/OrderBoard';
import OrderDetail from './components/OrderDetail';

export default function App() {
  return (
    <main className="content">
      <Routes>
        <Route path="/" element={<OrderBoard />} />
        <Route path="/orders/:id" element={<OrderDetail />} />
      </Routes>
    </main>
  );
}
