/**
 * App Utilities
 * Common functions for entire application (admin, customer, home, etc.)
 */

const AppUtils = {
    /**
     * Remove Vietnamese accents from string for search
     * @param {string} str - Input string
     * @returns {string} String without Vietnamese accents
     */
    removeVietnameseTones(str) {
        if (!str) return '';

        return str.toLowerCase()
            .replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a")
            .replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e")
            .replace(/ì|í|ị|ỉ|ĩ/g, "i")
            .replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o")
            .replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u")
            .replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y")
            .replace(/đ/g, "d");
    },

    /**
     * Search in table rows with Vietnamese accent support
     * @param {string} searchTerm - Search term
     * @param {string} tableSelector - CSS selector for table
     * @param {Function} callback - Optional callback after search
     */
    searchTable(searchTerm, tableSelector, callback) {
        const normalizedTerm = this.removeVietnameseTones(searchTerm);
        const rows = document.querySelectorAll(`${tableSelector} tbody tr`);

        rows.forEach(row => {
            const text = this.removeVietnameseTones(row.textContent);
            row.style.display = text.includes(normalizedTerm) ? '' : 'none';
        });

        if (callback) callback();
    },

    /**
     * Search in any elements with Vietnamese accent support
     * @param {string} searchTerm - Search term
     * @param {string} itemsSelector - CSS selector for items to search
     * @param {Function} callback - Optional callback after search
     */
    searchItems(searchTerm, itemsSelector, callback) {
        const normalizedTerm = this.removeVietnameseTones(searchTerm);
        const items = document.querySelectorAll(itemsSelector);

        items.forEach(item => {
            const text = this.removeVietnameseTones(item.textContent);
            item.style.display = text.includes(normalizedTerm) ? '' : 'none';
        });

        if (callback) callback();
    },

    /**
     * Format number as Vietnamese currency
     * @param {number} amount - Amount to format
     * @returns {string} Formatted currency string
     */
    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    },

    /**
     * Format number with thousand separators
     * @param {number} num - Number to format
     * @returns {string} Formatted number
     */
    formatNumber(num) {
        return new Intl.NumberFormat('vi-VN').format(num);
    },

    /**
     * Show alert message
     * @param {string} message - Message to show
     * @param {string} type - Type: success, error, warning, info
     */
    showAlert(message, type = 'info') {
        const alert = document.createElement('div');
        alert.className = `alert alert-${type}`;
        alert.textContent = message;
        alert.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px;';

        document.body.appendChild(alert);

        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.3s';
            setTimeout(() => alert.remove(), 300);
        }, 3000);
    },

    /**
     * Confirm dialog
     * @param {string} message - Confirmation message
     * @returns {boolean} User's choice
     */
    confirm(message) {
        return confirm(message);
    }
};

// Make it globally available
window.AppUtils = AppUtils;