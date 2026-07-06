<template>
    <div class="min-h-screen pb-12 bg-transparent font-sans text-[#2c3e50]">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- 1. Date Filter & Header -->
                <div
                    class="flex flex-col md:flex-row justify-between items-center bg-white rounded-2xl p-4 shadow-sm border border-gray-100/50">
                    <h1 class="text-lg font-bold text-[#2c3e50] mb-3 md:mb-0 flex items-center gap-2">
                        数据概览
                        <span class="text-xs font-normal text-gray-400 bg-gray-100 px-2 py-0.5 rounded">更新于 {{
                            updateTime }}</span>
                    </h1>
                    <div class="flex flex-wrap items-center justify-end gap-2">
                        <div class="flex bg-gray-50 p-1 rounded-lg">
                            <button v-for="range in dateRanges" :key="range" @click="activeRange = range"
                                :class="['px-4 py-1.5 rounded-md text-xs font-bold transition-all', activeRange === range ? 'bg-white text-[#4a8b6e] shadow-sm' : 'text-gray-500 hover:text-gray-700']">
                                {{ range }}
                            </button>
                        </div>
                        <button @click="refreshData"
                            class="px-3 py-1.5 rounded-lg text-xs font-bold border border-gray-200 text-gray-600 hover:text-[#4a8b6e] hover:border-[#4a8b6e]/40 hover:bg-[#f0fdf7] transition-colors">
                            刷新
                        </button>
                        <button @click="exportCsv"
                            class="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#2c3e50] text-white hover:bg-[#1f2f3d] transition-colors">
                            导出 CSV
                        </button>
                    </div>
                </div>

                <div v-if="errorMsg"
                    class="bg-[#fff7ed] border border-[#fed7aa] text-[#9a3412] rounded-xl px-4 py-3 text-sm flex items-center justify-between">
                    <span>{{ errorMsg }}</span>
                    <button @click="fetchData" class="font-bold hover:underline">重试</button>
                </div>

                <!-- 2. Key Metrics Cards -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <!-- GMV -->
                    <div
                        class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col justify-between h-36 relative overflow-hidden group cursor-pointer"
                        @click="goToOrderManage('COMPLETED')">
                        <div
                            class="absolute right-[-10px] top-[-10px] w-20 h-20 bg-[#4a8b6e]/5 rounded-full blur-xl group-hover:bg-[#4a8b6e]/10 transition-colors">
                        </div>
                        <div>
                            <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">成交金额 (GMV)</div>
                            <div class="text-3xl font-bold text-[#2c3e50] font-mono">¥{{ isLoading ? '--' : formatNumber(stats.gmv) }}
                            </div>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span
                                :class="['font-bold px-1.5 rounded flex items-center gap-0.5', stats.gmvGrowth >= 0 ? 'text-[#4a8b6e] bg-[#4a8b6e]/10' : 'text-red-500 bg-red-50']">
                                <component :is="stats.gmvGrowth >= 0 ? TrendingUp : TrendingDown" :size="12" /> {{
                                    Math.abs(stats.gmvGrowth).toFixed(1) }}%
                            </span>
                            <span class="text-gray-400">较上周期</span>
                        </div>
                    </div>

                    <!-- Visitors -->
                    <div
                        class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col justify-between h-36 relative overflow-hidden group cursor-pointer"
                        @click="goToProducts('sales')">
                        <div
                            class="absolute right-[-10px] top-[-10px] w-20 h-20 bg-blue-50 rounded-full blur-xl group-hover:bg-blue-100 transition-colors">
                        </div>
                        <div>
                            <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">访客数 (UV)</div>
                            <div class="text-3xl font-bold text-[#2c3e50] font-mono">{{ isLoading ? '--' : formatNumber(stats.visitorCount)
                                }}</div>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span
                                :class="['font-bold px-1.5 rounded flex items-center gap-0.5', stats.visitorGrowth >= 0 ? 'text-blue-500 bg-blue-50' : 'text-red-500 bg-red-50']">
                                <component :is="stats.visitorGrowth >= 0 ? TrendingUp : TrendingDown" :size="12" /> {{
                                    Math.abs(stats.visitorGrowth).toFixed(1) }}%
                            </span>
                            <span class="text-gray-400">较上周期</span>
                        </div>
                    </div>

                    <!-- Orders -->
                    <div
                        class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col justify-between h-36 relative overflow-hidden group cursor-pointer"
                        @click="goToOrderManage('COMPLETED')">
                        <div
                            class="absolute right-[-10px] top-[-10px] w-20 h-20 bg-orange-50 rounded-full blur-xl group-hover:bg-orange-100 transition-colors">
                        </div>
                        <div>
                            <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">成交订单</div>
                            <div class="text-3xl font-bold text-[#2c3e50] font-mono">{{ isLoading ? '--' : formatNumber(stats.orderCount)
                                }}</div>
                        </div>
                        <div class="flex items-center gap-2 text-xs">
                            <span
                                :class="['font-bold px-1.5 rounded flex items-center gap-0.5', stats.orderGrowth >= 0 ? 'text-orange-500 bg-orange-50' : 'text-red-500 bg-red-50']">
                                <component :is="stats.orderGrowth >= 0 ? TrendingUp : TrendingDown" :size="12" /> {{
                                    Math.abs(stats.orderGrowth).toFixed(1) }}%
                            </span>
                            <span class="text-gray-400">较上周期</span>
                        </div>
                    </div>
                </div>

                <!-- 3. Trend Chart (Large) -->
                <section class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm">
                    <div class="flex justify-between items-center mb-6">
                        <h2 class="font-bold text-lg text-[#2c3e50]">趋势分析</h2>
                        <div class="flex items-center gap-4 text-xs">
                            <div class="flex items-center gap-1.5">
                                <span class="w-2.5 h-2.5 rounded-full bg-[#4a8b6e]"></span>
                                <span class="text-gray-500">成交额</span>
                            </div>
                            <div class="flex items-center gap-1.5">
                                <span class="w-2.5 h-2.5 rounded-full bg-blue-400"></span>
                                <span class="text-gray-500">访客数</span>
                            </div>
                        </div>
                    </div>
                    <div class="h-64 w-full">
                        <div v-if="isLoading" class="h-full rounded-xl bg-gradient-to-r from-gray-50 via-gray-100 to-gray-50 animate-pulse"></div>
                        <div v-else-if="!hasTrendData" class="h-full rounded-xl border border-dashed border-gray-200 flex items-center justify-center text-sm text-gray-400">
                            当前时间范围暂无趋势数据
                        </div>
                        <canvas v-show="!isLoading && hasTrendData" id="trendChart"></canvas>
                    </div>
                </section>

                <div class="flex flex-col gap-6">

                    <!-- 4. Product Ranking (Full Width) -->
                    <section class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm flex flex-col">
                        <div class="flex justify-between items-center mb-4">
                            <h2 class="font-bold text-lg text-[#2c3e50]">商品排行榜</h2>
                            <span class="text-xs text-gray-400">按浏览量</span>
                        </div>

                        <div v-if="isLoading" class="flex-1 space-y-3">
                            <div v-for="i in 3" :key="i" class="h-12 rounded-xl bg-gradient-to-r from-gray-50 via-gray-100 to-gray-50 animate-pulse"></div>
                        </div>

                        <div v-else-if="topProducts.length === 0"
                            class="flex-1 rounded-xl border border-dashed border-gray-200 text-sm text-gray-400 flex items-center justify-center py-10">
                            暂无商品表现数据
                        </div>

                        <div v-else class="flex-1 overflow-y-auto space-y-4 pr-1">
                            <div v-for="(item, index) in topProducts" :key="index"
                                class="flex items-center gap-3 p-2 hover:bg-gray-50 rounded-xl transition-colors">
                                <div class="w-6 text-center font-bold text-sm"
                                    :class="index < 3 ? 'text-[#ff5e57]' : 'text-gray-400'">{{ index + 1 }}</div>
                                <div
                                    class="w-10 h-10 bg-gray-100 rounded-lg overflow-hidden border border-gray-100 flex-shrink-0">
                                    <img :src="item.image" class="w-full h-full object-cover" />
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="text-xs font-bold text-[#2c3e50] truncate">{{ item.title }}</div>
                                    <div class="text-[10px] text-gray-400">库存 {{ item.stock }}</div>
                                </div>
                                <div class="text-right">
                                    <div class="text-sm font-bold text-[#2c3e50] font-mono">{{ formatNumber(item.views)
                                        }}</div>
                                    <div class="text-[10px] text-gray-400">浏览</div>
                                </div>
                            </div>
                        </div>

                        <button @click="goToProducts('sales')"
                            class="w-full mt-4 text-xs text-gray-500 border border-gray-200 rounded-lg py-2 hover:bg-gray-50 hover:text-[#4a8b6e] transition-colors">
                            查看全部商品数据
                        </button>
                    </section>

                </div>

        </main>

    </div>
</template>

<script setup>
import request from '@/utils/request';
import Chart from 'chart.js/auto';
import {
    TrendingDown,
    TrendingUp
} from 'lucide-vue-next';
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const activeRange = ref('近7日');
const dateRanges = ['今日', '昨日', '近7日', '近30日'];
const updateTime = ref(new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }));
const isLoading = ref(false);
const errorMsg = ref('');

const stats = ref({
    gmv: 0,
    orderCount: 0,
    visitorCount: 0,
    gmvGrowth: 0,
    orderGrowth: 0,
    visitorGrowth: 0,
    trend: [],
    topProducts: []
});

const topProducts = computed(() =>
    (stats.value.topProducts || []).filter(item => Number(item?.stock || 0) > 0)
);
const hasTrendData = computed(() => Array.isArray(stats.value.trend) && stats.value.trend.length > 0);

const formatNumber = (num) => {
    return new Intl.NumberFormat().format(num);
};

let chartInstance = null;

const initChart = () => {
    const ctx = document.getElementById('trendChart');
    if (!ctx) return;

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '成交额',
                data: [],
                borderColor: '#4a8b6e',
                backgroundColor: 'rgba(74, 139, 110, 0.1)',
                tension: 0.4,
                fill: true,
                pointRadius: 4,
                pointHoverRadius: 6,
                yAxisID: 'y'
            }, {
                label: '访客数',
                data: [],
                borderColor: '#60a5fa', // Blue-400
                backgroundColor: 'transparent',
                borderDash: [5, 5],
                tension: 0.4,
                pointRadius: 0,
                pointHoverRadius: 4,
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    backgroundColor: '#2c3e50',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 10,
                    cornerRadius: 8
                }
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    beginAtZero: true,
                    grid: { color: '#f3f4f6' },
                    ticks: { color: '#9ca3af', font: { size: 10 } }
                },
                y1: {
                    type: 'linear',
                    display: false, // Hide secondary axis labels to keep it clean, or show if needed
                    position: 'right',
                    beginAtZero: true,
                    grid: { display: false }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: '#9ca3af', font: { size: 10 } }
                }
            }
        }
    });
};

const updateChart = () => {
    if (!chartInstance) return;

    const labels = stats.value.trend.map(t => t.date);
    const gmvData = stats.value.trend.map(t => t.gmv);
    const visitorData = stats.value.trend.map(t => t.visitors);

    chartInstance.data.labels = labels;
    chartInstance.data.datasets[0].data = gmvData;
    chartInstance.data.datasets[1].data = visitorData;
    chartInstance.update();
};

const goToOrderManage = (status) => {
    router.push({ name: 'OrderManage', query: { status } });
};

const goToProducts = (tab = 'sales') => {
    router.push({ path: '/my-products', query: { tab } });
};

const exportCsv = () => {
    const lines = [];
    lines.push('指标,数值');
    lines.push(`成交金额(GMV),${stats.value.gmv ?? 0}`);
    lines.push(`成交订单,${stats.value.orderCount ?? 0}`);
    lines.push(`访客数(UV),${stats.value.visitorCount ?? 0}`);
    lines.push('');
    lines.push('日期,成交额,访客数');
    (stats.value.trend || []).forEach((row) => {
        lines.push(`${row.date},${row.gmv ?? 0},${row.visitors ?? 0}`);
    });
    lines.push('');
    lines.push('商品ID,商品标题,价格,浏览量');
    (stats.value.topProducts || []).forEach((p) => {
        lines.push(`${p.id},"${(p.title || '').replace(/"/g, '""')}",${p.price ?? 0},${p.views ?? 0}`);
    });

    const blob = new Blob([`\uFEFF${lines.join('\n')}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const date = new Date().toISOString().slice(0, 10);
    link.href = url;
    link.download = `seller-data-${date}.csv`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
};

const refreshData = () => {
    fetchData();
};

const fetchData = async () => {
    isLoading.value = true;
    errorMsg.value = '';
    try {
        const res = await request.get('/statistics/seller', {
            params: { range: activeRange.value }
        });
        stats.value = res;
        updateTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        await nextTick();
        updateChart();
    } catch (e) {
        console.error('Failed to fetch statistics:', e);
        errorMsg.value = '数据加载失败，请检查网络或稍后重试';
    } finally {
        isLoading.value = false;
    }
};

watch(activeRange, () => {
    fetchData();
});

onMounted(() => {
    initChart();
    fetchData();
});

onBeforeUnmount(() => {
    if (chartInstance) {
        chartInstance.destroy();
        chartInstance = null;
    }
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
</style>
