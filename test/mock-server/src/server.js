const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Load endpoints configuration
let endpointsConfig;
try {
  const configPath = path.join(__dirname, '..', 'config', 'endpoints.json');
  endpointsConfig = JSON.parse(fs.readFileSync(configPath, 'utf8'));
  console.log('✓ Endpoints configuration loaded successfully');
} catch (error) {
  console.error('✗ Error loading endpoints configuration:', error.message);
  process.exit(1);
}

// Middleware for request logging
app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  const method = req.method;
  const path = req.path;
  const userAgent = req.get('User-Agent') || 'Unknown';
  const ip = req.ip || req.connection.remoteAddress;
  
  console.log(`[${timestamp}] ${method} ${path} - ${userAgent} - ${ip}`);
  next();
});

// Handle configured endpoints
app.use((req, res, next) => {
  const endpoint = endpointsConfig[req.path];
  
  if (!endpoint) {
    return next(); // Pass to 404 handler
  }
  
  const delay = endpoint.delay || 0;
  
  // Apply delay if specified
  setTimeout(() => {
    try {
      // Set custom headers
      if (endpoint.headers) {
        Object.keys(endpoint.headers).forEach(header => {
          res.setHeader(header, endpoint.headers[header]);
        });
      }
      
      // Set status code
      res.status(endpoint.statusCode || 200);
      
      // Read and send response file
      const responsePath = path.join(__dirname, '..', endpoint.responseFile);
      
      if (!fs.existsSync(responsePath)) {
        console.error(`✗ Response file not found: ${responsePath}`);
        return res.status(500).json({ 
          error: 'Response file not found',
          file: endpoint.responseFile 
        });
      }
      
      const responseContent = fs.readFileSync(responsePath, 'utf8');
      
      // Send appropriate content type based on file extension
      const fileExt = path.extname(responsePath).toLowerCase();
      if (fileExt === '.json') {
        res.json(JSON.parse(responseContent));
      } else {
        res.send(responseContent);
      }
      
    } catch (error) {
      console.error(`✗ Error serving ${req.path}:`, error.message);
      res.status(500).json({ 
        error: 'Internal server error',
        message: error.message 
      });
    }
  }, delay);
});

// 404 handler for undefined endpoints
app.use((req, res) => {
  console.log(`⚠ 404 - Endpoint not found: ${req.path}`);
  
  try {
    const errorPath = path.join(__dirname, '..', 'responses', 'error404.html');
    
    if (fs.existsSync(errorPath)) {
      res.status(404);
      res.setHeader('Content-Type', 'text/html; charset=utf-8');
      const errorContent = fs.readFileSync(errorPath, 'utf8');
      res.send(errorContent);
    } else {
      res.status(404).json({ 
        error: 'Not Found',
        message: `Endpoint ${req.path} not found`,
        availableEndpoints: Object.keys(endpointsConfig)
      });
    }
  } catch (error) {
    console.error('✗ Error serving 404 page:', error.message);
    res.status(404).json({ 
      error: 'Not Found',
      message: `Endpoint ${req.path} not found`
    });
  }
});

// Error handling middleware
app.use((error, req, res, next) => {
  console.error('✗ Unhandled error:', error);
  res.status(500).json({
    error: 'Internal Server Error',
    message: 'An unexpected error occurred'
  });
});

// Start server
app.listen(PORT, () => {
  console.log('='.repeat(50));
  console.log(`🚀 Mock HTTP Server started`);
  console.log(`📡 Listening on port: ${PORT}`);
  console.log(`🌐 Server URL: http://localhost:${PORT}`);
  console.log('📋 Configured endpoints:');
  
  Object.keys(endpointsConfig).forEach(endpoint => {
    const config = endpointsConfig[endpoint];
    const delay = config.delay ? ` (${config.delay}ms delay)` : '';
    console.log(`   • ${endpoint} -> ${config.responseFile}${delay}`);
  });
  
  console.log('='.repeat(50));
  console.log('💡 Test commands:');
  console.log(`   curl http://localhost:${PORT}/`);
  console.log(`   curl http://localhost:${PORT}/products`);
  console.log(`   curl http://localhost:${PORT}/api/data`);
  console.log('='.repeat(50));
});

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\n🛑 Shutting down Mock HTTP Server...');
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\n🛑 Shutting down Mock HTTP Server...');
  process.exit(0);
});