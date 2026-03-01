const express = require('express');
const axios = require('axios');
const app = express();

app.set('view engine', 'ejs');

app.get('/', async (req, res) => {
    // Simulating context from user's phone sensors
    const userContext = req.query.context || "I am rushing to a meeting";
    const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';

    try {
        const response = await axios.get(`${backendUrl}/api/ui?context=${encodeURIComponent(userContext)}`);
        res.render('index', { components: response.data, context: userContext });
    } catch (error) {
        console.error(error);
        res.render('index', { components: [], context: "Error connecting to Superapp Brain" });
    }
});

app.listen(3000, () => console.log('Frontend listening on port 3000'));