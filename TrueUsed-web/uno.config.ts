import { defineConfig, presetAttributify, presetIcons, presetUno } from 'unocss'

// 主题：现代简约风格，避免过于鲜艳的颜色
export default defineConfig({
    presets: [presetUno(), presetAttributify(), presetIcons()],
    theme: {
        colors: {
            // 使用更柔和、专业的色调
            primary: '#4CAF50', // Material Green
            primaryDark: '#388E3C',
            primaryLight: '#81C784',

            // 中性色
            dark: '#1f2937', // Gray 800
            secondary: '#6b7280', // Gray 500
            light: '#F7F8FC', // Page Background

            // 状态色 - 稍微降低饱和度
            success: '#10b981',
            warning: '#FFB300', // Amber
            danger: '#ef4444',
            info: '#3b82f6',
        },
    },
    shortcuts: [
        // 卡片统一风格 - 减少阴影，更扁平化
        ['card', 'bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow duration-300'],

        // 按钮风格 - 扁平化，无渐变
        ['btn-primary', 'inline-flex items-center justify-center px-4 py-2 bg-primary text-white rounded-lg hover:bg-primaryDark transition-colors duration-200 font-medium'],
        ['btn-secondary', 'inline-flex items-center justify-center px-4 py-2 bg-white text-gray-700 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium'],
        ['btn-ghost', 'inline-flex items-center justify-center px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors duration-200'],

        // 布局辅助
        ['flex-center', 'flex items-center justify-center'],
        ['flex-between', 'flex items-center justify-between'],

        // 文本样式
        ['text-h1', 'text-2xl font-bold text-gray-900'],
        ['text-h2', 'text-xl font-bold text-gray-900'],
        ['text-h3', 'text-lg font-semibold text-gray-900'],
        ['text-body', 'text-base text-gray-600'],
        ['text-sm-muted', 'text-sm text-gray-500'],

        // 标签/Chip - 柔和背景
        ['chip', 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium'],
        ['chip-primary', 'bg-green-50 text-green-700'],
        ['chip-success', 'bg-green-50 text-green-700'],
        ['chip-warning', 'bg-yellow-50 text-yellow-700'],
        ['chip-danger', 'bg-red-50 text-red-700'],
        ['chip-gray', 'bg-gray-100 text-gray-700'],

        // 头像
        ['avatar', 'rounded-full object-cover bg-gray-100'],
    ],
})
