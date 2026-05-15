import axios from 'axios';

const api = axios.create({
    baseURL: '/api' // El Proxy de Vite hace que esto apunte a localhost:8080/api automáticamente
});

export const analyzeScrim = async (files) => {
    const formData = new FormData();
    formData.append('scoreboard', files.scoreboard);
    formData.append('performance', files.performance);
    formData.append('timeline', files.timeline);

    return api.post('/matches/analyze', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

export const getMatches = async () => {
    const response = await api.get('/matches');
    return response.data;
};