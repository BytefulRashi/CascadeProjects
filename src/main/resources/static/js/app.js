document.addEventListener('DOMContentLoaded', function() {
    // Cache DOM elements
    const elements = {
        sourceType: document.getElementById('sourceType'),
        clickhouseConfig: document.getElementById('clickhouse-config'),
        fileConfig: document.getElementById('file-config'),
        columnSelection: document.getElementById('column-selection'),
        previewSection: document.getElementById('preview-section'),
        statusSection: document.getElementById('status-section'),
        loading: document.getElementById('loading'),
        progressBar: document.getElementById('progress-bar'),
        statusMessage: document.getElementById('status-message'),
        columnsListDiv: document.getElementById('columns-list'),
        previewTable: document.getElementById('preview-table')
    };

    // Add event listeners
    elements.sourceType.addEventListener('change', handleSourceTypeChange);
    document.getElementById('test-connection').addEventListener('click', testClickHouseConnection);
    document.getElementById('load-tables').addEventListener('click', loadClickHouseTables);
    document.getElementById('tables').addEventListener('change', loadClickHouseColumns);
    document.getElementById('load-file-columns').addEventListener('click', loadFileColumns);
    document.getElementById('preview-data').addEventListener('click', previewData);
    document.getElementById('start-ingestion').addEventListener('click', startIngestion);
    document.getElementById('select-all-columns').addEventListener('click', () => toggleAllColumns(true));
    document.getElementById('deselect-all-columns').addEventListener('click', () => toggleAllColumns(false));

    // Utility functions
    function showLoading() {
        elements.loading.classList.remove('hidden');
    }

    function hideLoading() {
        elements.loading.classList.add('hidden');
    }

    function showError(message) {
        elements.statusMessage.className = 'alert alert-danger mt-3';
        elements.statusMessage.textContent = message;
        elements.statusSection.classList.remove('hidden');
    }

    function showSuccess(message) {
        elements.statusMessage.className = 'alert alert-success mt-3';
        elements.statusMessage.textContent = message;
        elements.statusSection.classList.remove('hidden');
    }

    function updateProgress(percent) {
        elements.progressBar.style.width = `${percent}%`;
        elements.progressBar.setAttribute('aria-valuenow', percent);
    }

    function getClickHouseConfig() {
        return {
            host: document.getElementById('host').value,
            port: parseInt(document.getElementById('port').value),
            database: document.getElementById('database').value,
            user: document.getElementById('user').value,
            jwtToken: document.getElementById('jwt').value,
            table: document.getElementById('tables').value
        };
    }

    function getSelectedColumns() {
        return Array.from(document.querySelectorAll('#columns-list input:checked'))
            .map(checkbox => checkbox.value);
    }

    function toggleAllColumns(checked) {
        document.querySelectorAll('#columns-list input[type="checkbox"]')
            .forEach(checkbox => checkbox.checked = checked);
    }

    // Event handlers
    function handleSourceTypeChange() {
        const selectedSource = elements.sourceType.value;
        elements.clickhouseConfig.classList.toggle('hidden', selectedSource !== 'clickhouse');
        elements.fileConfig.classList.toggle('hidden', selectedSource !== 'file');
        elements.columnSelection.classList.add('hidden');
        elements.previewSection.classList.add('hidden');
        elements.statusSection.classList.add('hidden');
    }

    async function testClickHouseConnection() {
        showLoading();
        try {
            const response = await fetch('/api/clickhouse/test', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(getClickHouseConfig())
            });
            
            const data = await response.json();
            if (response.ok) {
                showSuccess('Connection successful!');
            } else {
                showError('Connection failed: ' + data.error);
            }
        } catch (error) {
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    async function loadClickHouseTables() {
        showLoading();
        try {
            const response = await fetch('/api/clickhouse/tables', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(getClickHouseConfig())
            });
            
            const data = await response.json();
            if (response.ok) {
                const tablesSelect = document.getElementById('tables');
                tablesSelect.innerHTML = '<option value="">Select Table</option>' +
                    data.map(table => `<option value="${table}">${table}</option>`).join('');
                document.getElementById('tables-section').classList.remove('hidden');
                showSuccess('Tables loaded successfully');
            } else {
                showError('Failed to load tables: ' + data.error);
            }
        } catch (error) {
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    function displayColumns(columns) {
        elements.columnsListDiv.innerHTML = columns.map(column => `
            <div class="col-md-4">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="${column}" id="col-${column}">
                    <label class="form-check-label" for="col-${column}">${column}</label>
                </div>
            </div>
        `).join('');
        
        elements.columnSelection.classList.remove('hidden');
    }

    async function loadClickHouseColumns() {
        if (!document.getElementById('tables').value) {
            showError('Please select a table first');
            return;
        }

        showLoading();
        try {
            const response = await fetch('/api/clickhouse/columns', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(getClickHouseConfig())
            });
            
            const data = await response.json();
            if (response.ok) {
                displayColumns(data);
                showSuccess('Columns loaded successfully');
            } else {
                showError('Failed to load columns: ' + data.error);
            }
        } catch (error) {
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    async function loadFileColumns() {
        const file = document.getElementById('file').files[0];
        if (!file) {
            showError('Please select a file first');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);
        formData.append('delimiter', document.getElementById('delimiter').value);

        showLoading();
        try {
            const response = await fetch('/api/file/columns', {
                method: 'POST',
                body: formData
            });
            
            const data = await response.json();
            if (response.ok) {
                displayColumns(data);
                showSuccess('Columns loaded successfully');
            } else {
                showError('Failed to load columns: ' + data.error);
            }
        } catch (error) {
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    function displayPreviewTable(data) {
        if (!data || data.length === 0) {
            elements.previewTable.innerHTML = '<div class="alert alert-info">No data to preview</div>';
            return;
        }

        const columns = Object.keys(data[0]);
        const table = `
            <table class="table table-striped table-bordered">
                <thead>
                    <tr>${columns.map(col => `<th>${col}</th>`).join('')}</tr>
                </thead>
                <tbody>
                    ${data.map(row => `
                        <tr>${columns.map(col => `<td>${row[col] || ''}</td>`).join('')}</tr>
                    `).join('')}
                </tbody>
            </table>
        `;
        
        elements.previewTable.innerHTML = table;
        elements.previewSection.classList.remove('hidden');
    }

    async function previewData() {
        const selectedColumns = getSelectedColumns();
        if (selectedColumns.length === 0) {
            showError('Please select at least one column');
            return;
        }

        showLoading();
        try {
            let response;
            if (elements.sourceType.value === 'clickhouse') {
                response = await fetch('/api/clickhouse/preview?' + new URLSearchParams({
                    columns: selectedColumns
                }), {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(getClickHouseConfig())
                });
            } else {
                const formData = new FormData();
                formData.append('file', document.getElementById('file').files[0]);
                formData.append('delimiter', document.getElementById('delimiter').value);
                selectedColumns.forEach(col => formData.append('columns', col));
                
                response = await fetch('/api/file/preview', {
                    method: 'POST',
                    body: formData
                });
            }
            
            const data = await response.json();
            if (response.ok) {
                displayPreviewTable(data);
                showSuccess('Preview generated successfully');
            } else {
                showError('Failed to preview data: ' + data.error);
            }
        } catch (error) {
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }

    async function startIngestion() {
        const selectedColumns = getSelectedColumns();
        if (selectedColumns.length === 0) {
            showError('Please select at least one column');
            return;
        }

        showLoading();
        updateProgress(0);
        try {
            let response;
            if (elements.sourceType.value === 'clickhouse') {
                // ClickHouse to File
                response = await fetch('/api/ingest/clickhouse-to-file?' + new URLSearchParams({
                    columns: selectedColumns
                }), {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(getClickHouseConfig())
                });
            } else {
                // File to ClickHouse
                const formData = new FormData();
                formData.append('file', document.getElementById('file').files[0]);
                formData.append('delimiter', document.getElementById('delimiter').value);
                selectedColumns.forEach(col => formData.append('columns', col));
                
                response = await fetch('/api/ingest/file-to-clickhouse', {
                    method: 'POST',
                    body: formData
                });
            }
            
            const result = await response.json();
            if (response.ok) {
                updateProgress(100);
                showSuccess(`Ingestion completed successfully! ${result.recordCount ? `Processed ${result.recordCount} records.` : ''}`);
            } else {
                throw new Error(result.error);
            }
        } catch (error) {
            updateProgress(0);
            showError('Error: ' + error.message);
        } finally {
            hideLoading();
        }
    }
});