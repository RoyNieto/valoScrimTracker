import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
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