import { useState, useEffect } from 'react';
import { analyzeScrim, getMatches } from './services/api';

export default function App() {
  const [view, setView] = useState('upload'); // upload, history, details, insights
  const [files, setFiles] = useState({ scoreboard: null, performance: null, timeline: null });
  const [status, setStatus] = useState('idle');
  const [matches, setMatches] = useState([]);
  const [selectedMatch, setSelectedMatch] = useState(null);

  useEffect(() => {
    if (view === 'history' || view === 'insights') {
      getMatches().then(setMatches).catch(console.error);
    }
  }, [view]);

  const handleUpload = async () => {
    setStatus('loading');
    try {
      await analyzeScrim(files);
      setStatus('success');
      setTimeout(() => {
        setFiles({ scoreboard: null, performance: null, timeline: null });
        setStatus('idle');
        setView('history');
      }, 1500);
    } catch (error) {
      setStatus('error');
    }
  };

  // --- LÓGICA DE INSIGHTS (Cálculos rápidos) ---
  const getMapStats = () => {
    const stats = {};
    matches.forEach(m => {
      if (!stats[m.mapName]) {
        stats[m.mapName] = { 
          count: 0, wins: 0,
          atkWon: 0, atkPlayed: 0,
          defWon: 0, defPlayed: 0,
          pistolAtkWon: 0, pistolDefWon: 0
        };
      }
      const s = stats[m.mapName];
      s.count++;
      if ((m.roundsWonAtk + m.roundsWonDef) > (m.totalRounds / 2)) s.wins++;
      
      s.atkWon += m.roundsWonAtk;
      s.atkPlayed += 12; // Regla: Primer bando siempre 12
      
      s.defWon += m.roundsWonDef;
      s.defPlayed += (m.totalRounds - 12); // Regla: Segundo bando es el resto
      
      if (m.wonPistolAtk) s.pistolAtkWon++;
      if (m.wonPistolDef) s.pistolDefWon++;
    });
    return stats;
  };
  const StatBar = ({ label, current, total, colorClass }) => {
    const percentage = total > 0 ? Math.round((current / total) * 100) : 0;
    return (
      <div className="mb-3">
        <div className="flex justify-between text-[10px] mb-1 font-bold uppercase tracking-widest text-gray-400">
          <span>{label}</span>
          <span className={colorClass}>{percentage}%</span>
        </div>
        <div className="h-1.5 w-full bg-gray-800 rounded-full overflow-hidden">
          <div 
            className={`h-full transition-all duration-1000 ${colorClass.replace('text', 'bg')}`} 
            style={{ width: `${percentage}%` }}
          ></div>
        </div>
      </div>
    );
  };
  const getPlayerStats = () => {
    const stats = {};
    matches.forEach(m => {
      m.playerStats?.forEach(p => {
        const name = p.player?.name || "Desconocido";
        if (!stats[name]) stats[name] = { games: 0, kills: 0, deaths: 0, acs: 0 };
        stats[name].games++;
        stats[name].kills += p.kills;
        stats[name].deaths += p.deaths;
        stats[name].acs += p.acs;
      });
    });
    return stats;
  };

  return (
    <div className="min-h-screen bg-[#0f1923] text-white p-4 md:p-8">
      {/* Navegación Estilo Valorant */}
      <nav className="flex flex-wrap justify-between items-center mb-8 border-b border-gray-800 pb-4 gap-4">
        <h1 className="text-2xl font-bold text-[#ff4655] tracking-tighter uppercase">Peekaboo</h1>
        <div className="flex gap-2">
          {['upload', 'history', 'insights'].map(v => (
            <button key={v} onClick={() => setView(v)} 
              className={`px-4 py-1 text-sm font-bold uppercase border-b-2 transition-all ${view === v ? 'border-[#ff4655] text-[#ff4655]' : 'border-transparent text-gray-500 hover:text-white'}`}>
              {v === 'upload' ? 'Ingesta' : v === 'history' ? 'Partidas' : 'Estadísticas'}
            </button>
          ))}
        </div>
      </nav>

      {/* 1. UPLOADER */}
      {view === 'upload' && (
        <div className="max-w-xl mx-auto bg-[#1a252e] p-6 rounded border-l-4 border-[#ff4655]">
          <h2 className="text-xl font-bold mb-6 uppercase italic">Analizar Scrim</h2>
          <div className="space-y-3 mb-6">
            {['scoreboard', 'performance', 'timeline'].map(t => (
              <div key={t} className="bg-[#0f1923] p-3 rounded flex justify-between items-center border border-gray-800">
                <span className="text-xs font-black uppercase text-gray-400">{t}</span>
                <input type="file" onChange={(e) => setFiles(prev => ({...prev, [t]: e.target.files[0]}))} className="text-xs text-gray-500" />
              </div>
            ))}
          </div>
          {status === 'error' ? (
            <button onClick={handleUpload} className="w-full py-3 bg-yellow-500 text-black font-black uppercase rounded">Reintentar Conexión</button>
          ) : (
            <button onClick={handleUpload} disabled={status === 'loading' || !files.scoreboard} className="w-full py-3 bg-[#ff4655] font-black uppercase rounded disabled:opacity-50">
              {status === 'loading' ? 'Procesando...' : 'Subir y Analizar'}
            </button>
          )}
        </div>
      )}

      {/* 2. HISTORIAL (LISTA) */}
      {view === 'history' && (
        <div className="max-w-4xl mx-auto grid gap-4">
          {matches.map(m => (
            <div key={m.id} onClick={() => { setSelectedMatch(m); setView('details'); }} 
              className="bg-[#1a252e] p-4 rounded border border-gray-800 hover:border-[#ff4655] cursor-pointer flex justify-between items-center group">
              <div>
                <span className="text-[10px] text-gray-500 font-bold uppercase">{new Date(m.date).toLocaleDateString()}</span>
                <h3 className="text-lg font-black uppercase group-hover:text-[#ff4655]">{m.mapName}</h3>
              </div>
              <div className="flex items-center gap-6">
                <div className="text-center">
                  <p className="text-xs text-gray-500 uppercase font-bold">Resultado</p>
                  <p className="font-black text-xl">{m.roundsWonAtk + m.roundsWonDef} <span className="text-gray-600 text-sm">Rnds</span></p>
                </div>
                <div className="h-8 w-[2px] bg-gray-800"></div>
                <span className="text-[#ff4655] font-black">→</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 3. VISTA DETALLADA */}
      {view === 'details' && selectedMatch && (
        <div className="max-w-5xl mx-auto animate-in fade-in duration-500">
          <button onClick={() => setView('history')} className="mb-4 text-xs font-bold text-gray-500 hover:text-[#ff4655]">« VOLVER AL HISTORIAL</button>
          <div className="grid md:grid-cols-3 gap-6">
            <div className="md:col-span-1 bg-[#1a252e] p-6 rounded border-t-2 border-[#ff4655]">
              <h2 className="text-3xl font-black uppercase mb-1">{selectedMatch.mapName}</h2>
              <p className="text-gray-500 text-sm mb-6">Partida #{selectedMatch.id}</p>
              <div className="space-y-4">
                <div className="bg-[#0f1923] p-3 rounded">
                  <p className="text-[10px] text-gray-500 uppercase font-black">Pistolas Ganadas</p>
                  <div className="flex gap-4 mt-1">
                    <span className={selectedMatch.wonPistolAtk ? "text-green-400 font-bold" : "text-red-400 line-through"}>ATK</span>
                    <span className={selectedMatch.wonPistolDef ? "text-green-400 font-bold" : "text-red-400 line-through"}>DEF</span>
                  </div>
                </div>
                <div className="bg-[#0f1923] p-3 rounded">
                  <p className="text-[10px] text-gray-500 uppercase font-black">Control de Sitio</p>
                  <p className="text-sm mt-1">Spikes: <span className="text-[#ff4655]">{selectedMatch.spikesDeployedAtk}</span> Plantadas</p>
                  <p className="text-sm">Detonaciones: <span className="text-green-400">{selectedMatch.detonationsAtk}</span></p>
                </div>
              </div>
            </div>
            <div className="md:col-span-2 bg-[#1a252e] p-6 rounded overflow-x-auto">
              <h3 className="text-sm font-black uppercase text-gray-500 mb-4">Estadísticas de Jugadores</h3>
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-gray-800 text-gray-400">
                    <th className="pb-2">JUGADOR</th>
                    <th className="pb-2">AGENTE</th>
                    <th className="pb-2 text-center">ACS</th>
                    <th className="pb-2 text-center">KDA</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800">
                  {selectedMatch.playerStats.map((p, i) => (
                    <tr key={i} className="hover:bg-[#0f1923] transition-colors">
                      <td className="py-3 font-bold">{p.player?.name || p.playerName}</td>
                      <td className="py-3 uppercase text-xs text-gray-400">{p.agent}</td>
                      <td className="py-3 text-center font-mono text-[#ff4655]">{p.acs}</td>
                      <td className="py-3 text-center font-mono">{p.kills}/{p.deaths}/{p.assists}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* 4. DASHBOARD GLOBAL */}
      {view === 'insights' && (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="grid md:grid-cols-2 gap-6">
        {Object.entries(getMapStats()).map(([map, s]) => (
          <div key={map} className="bg-[#1a252e] p-6 rounded border-b-2 border-gray-800 hover:border-[#ff4655] transition-all">
            <div className="flex justify-between items-end mb-6">
              <h3 className="text-2xl font-black uppercase italic text-[#ff4655]">{map}</h3>
              <span className="text-xs font-mono text-gray-500">{s.count} PARTIDAS ANALIZADAS</span>
            </div>

            <div className="grid grid-cols-2 gap-8">
              {/* Columna Winrates Generales */}
              <div>
                <StatBar label="Winrate Partidas" current={s.wins} total={s.count} colorClass="text-yellow-400" />
                <StatBar label="Rondas Ataque" current={s.atkWon} total={s.atkPlayed} colorClass="text-green-400" />
                <StatBar label="Rondas Defensa" current={s.defWon} total={s.defPlayed} colorClass="text-blue-400" />
              </div>

              {/* Columna Pistolas */}
              <div>
                <h4 className="text-[9px] font-black text-gray-600 uppercase mb-4 tracking-tighter">Performance Pistols</h4>
                <StatBar label="Pistolas ATK" current={s.pistolAtkWon} total={s.count} colorClass="text-cyan-400" />
                <StatBar label="Pistolas DEF" current={s.pistolDefWon} total={s.count} colorClass="text-purple-400" />
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {/* Ranking de jugadores (el que ya teníamos) */}
      <div className="bg-[#1a252e] p-6 rounded">
          <h3 className="text-sm font-black uppercase text-gray-500 mb-6 tracking-widest">Global MVP Leaderboard</h3>
          <div className="grid md:grid-cols-3 gap-4">
            {Object.entries(getPlayerStats()).sort((a,b) => b[1].acs - a[1].acs).map(([name, ps]) => (
              <div key={name} className="bg-[#0f1923] p-4 rounded border border-gray-800 flex justify-between items-center">
                <span className="font-bold text-sm uppercase">{name}</span>
                <div className="text-right">
                  <p className="text-[#ff4655] font-black text-sm">{Math.round(ps.acs/ps.games)} <span className="text-[10px] text-gray-600">ACS</span></p>
                </div>
              </div>
            ))}
          </div>
      </div>
    </div>
  )}
    </div>
  );
}