# ClickHouse Data Ingestion Tool

A modern web-based application for bidirectional data ingestion between ClickHouse databases and flat files (CSV). This tool provides an intuitive interface for data transfer operations with features like column selection, data preview, and progress tracking.

## 🚀 Features

- **Bidirectional Data Transfer**
  - ClickHouse to CSV export
  - CSV to ClickHouse import
- **Secure Authentication**
  - JWT token-based authentication for ClickHouse
  - Input validation and sanitization
- **User-Friendly Interface**
  - Modern, responsive Bootstrap 5 UI
  - Real-time progress tracking
  - Data preview functionality
- **Column Management**
  - Selective column import/export
  - Column type validation
  - Batch processing support
- **Error Handling**
  - Comprehensive error reporting
  - User-friendly error messages
  - Validation for file types and sizes

## 🛠️ Technology Stack

- **Backend**
  - Java 11+
  - Spring Boot 2.7+
  - ClickHouse JDBC Driver
  - Apache Commons CSV
  - Lombok
- **Frontend**
  - HTML5
  - Bootstrap 5
  - JavaScript (ES6+)
  - Bootstrap Icons

## 📋 Prerequisites

1. Java JDK 11 or higher
2. Maven 3.6 or higher
3. ClickHouse database instance
4. Modern web browser (Chrome, Firefox, Safari, Edge)

## ⚙️ Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd clickhouse-ingestion
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure application**
   - Update `application.properties` if needed (default port is 8080)

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - Open your browser and navigate to `http://localhost:8080`

## 🔧 Configuration

### ClickHouse Connection
```json
{
  "host": "your-clickhouse-host",
  "port": 8123,
  "database": "your-database",
  "user": "your-username",
  "jwtToken": "your-jwt-token"
}
```

### File Upload Limits
- Maximum file size: 100MB
- Supported formats: CSV
- Default delimiter: comma (,)

## 🎯 Usage Guide

1. **Source Selection**
   - Choose between ClickHouse or CSV file as your data source

2. **Configuration**
   - For ClickHouse: Enter connection details and test connection
   - For CSV: Upload file and specify delimiter

3. **Column Selection**
   - View available columns
   - Select columns for transfer
   - Preview data before transfer

4. **Data Transfer**
   - Click "Start Ingestion" to begin transfer
   - Monitor progress in real-time
   - View completion status and record count

## 🔒 Security Features

- JWT authentication for ClickHouse connections
- Input validation for all user inputs
- File type validation
- Size limit enforcement
- Secure error handling

## 🌟 Best Practices

1. **Data Transfer**
   - Preview data before transfer
   - Verify column mappings
   - Use appropriate delimiters for CSV files

2. **Performance**
   - Limit file sizes to under 100MB
   - Select only required columns
   - Use appropriate batch sizes

3. **Security**
   - Use strong JWT tokens
   - Regular token rotation
   - Validate input data

## 🐛 Troubleshooting

Common issues and solutions:

1. **Connection Failed**
   - Verify ClickHouse credentials
   - Check network connectivity
   - Ensure correct port configuration

2. **File Upload Issues**
   - Check file size limit
   - Verify file format (CSV only)
   - Ensure proper file permissions

3. **Data Preview Not Working**
   - Verify column selection
   - Check file encoding
   - Validate delimiter setting

## 📝 API Documentation

### ClickHouse Endpoints
- `POST /api/clickhouse/test` - Test connection
- `POST /api/clickhouse/tables` - Get available tables
- `POST /api/clickhouse/columns` - Get table columns
- `POST /api/clickhouse/preview` - Preview data

### File Endpoints
- `POST /api/file/columns` - Get file columns
- `POST /api/file/preview` - Preview file data

### Ingestion Endpoints
- `POST /api/ingest/clickhouse-to-file` - Export data
- `POST /api/ingest/file-to-clickhouse` - Import data

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- Your Name - *Initial work*

## 🙏 Acknowledgments

- ClickHouse Team
- Spring Boot Team
- Bootstrap Team
- All contributors
