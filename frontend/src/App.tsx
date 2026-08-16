import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css'
import Board from "./pages/Board.tsx";

function App() {

  return (
    <Router>
      <Routes>
        <Route path ="/" element = {<Board/>}/>
      </Routes>
    </Router>
  )
}

export default App
