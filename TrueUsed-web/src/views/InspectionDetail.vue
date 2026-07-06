<template>
    <div class="min-h-screen bg-[#f7f9fa] pb-12 font-sans text-[#2c3e50]">

        <!-- --- Top Navigation --- -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100 print:hidden">
            <div class="max-w-4xl mx-auto px-4 h-[60px] flex items-center justify-between">
                <div class="flex items-center gap-2 cursor-pointer text-gray-500 hover:text-gray-900 transition-colors"
                    @click="router.back()">
                    <div class="i-lucide-arrow-left text-xl"></div>
                    <span class="text-sm font-bold">返回列表</span>
                </div>
                <div class="font-bold text-lg">{{ route.query.type === 'order' ? '本单验货报告' : '平台验货报告' }}</div>
                <div class="flex items-center gap-3">
                    <button v-if="!reportUnavailable && report.id" @click="handleDownload"
                        class="bg-white border border-gray-200 text-gray-700 px-3 py-1.5 rounded-lg text-xs font-bold hover:bg-gray-50 transition-colors flex items-center gap-1.5 cursor-pointer">
                        <div class="i-lucide-download w-3.5 h-3.5"></div> 下载 PDF
                    </button>
                </div>
            </div>
        </nav>

        <main v-if="loading" class="max-w-4xl mx-auto p-10 text-center text-gray-400">
            加载中...
        </main>

        <main v-else-if="reportUnavailable" class="max-w-4xl mx-auto p-10 text-center text-gray-400">
            {{ unavailableMessage }}
        </main>

        <main v-else-if="!report.id" class="max-w-4xl mx-auto p-10 text-center text-gray-400">
            未找到该验货报告
        </main>

        <main v-else class="max-w-4xl mx-auto p-4 md:p-6 space-y-6">

            <!-- 1. Report Header / Certificate Badge -->
            <div class="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 relative overflow-hidden">
                <!-- Watermark Background -->
                <div
                    class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-[#4a8b6e]/5 to-transparent rounded-bl-full -mr-10 -mt-10 pointer-events-none">
                </div>

                <div class="relative z-10 flex flex-col md:flex-row gap-8 items-center md:items-start">
                    <!-- Product Image -->
                    <div
                        class="w-40 h-40 md:w-48 md:h-48 bg-gray-50 rounded-2xl flex-shrink-0 overflow-hidden border border-gray-100">
                        <img :src="report.image || 'https://images.unsplash.com/photo-1512054502232-10a0a035d672?auto=format&fit=crop&q=80&w=300'"
                            @error="onImageError"
                            class="w-full h-full object-cover" />
                    </div>

                    <!-- Info -->
                    <div class="flex-1 text-center md:text-left">
                        <div class="flex flex-col md:flex-row md:justify-between md:items-start mb-4">
                            <div>
                                <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ report.title || '未知商品' }}</h1>
                                <div class="flex flex-wrap gap-2 justify-center md:justify-start">
                                    <span class="bg-gray-100 text-gray-600 text-xs px-2 py-1 rounded font-medium">{{
                                        report.category || '通用类别' }}</span>
                                </div>
                            </div>

                            <!-- Overall Grade Badge -->
                            <div class="mt-4 md:mt-0 flex flex-col items-center">
                                <div
                                    :class="['w-16 h-20 flex flex-col items-center justify-center rounded-lg shadow-lg text-white font-bold mb-1', getGradeColor(report.grade)]">
                                    <span class="text-3xl leading-none">{{ report.grade || 'A' }}</span>
                                    <span class="text-xs opacity-80">综合评级</span>
                                </div>
                                <span class="text-xs font-bold text-[#4a8b6e]"
                                    v-if="(report.grade || 'A') !== 'X'">认证通过</span>
                                <span class="text-xs font-bold text-red-500" v-else>认证不通过</span>
                            </div>
                        </div>

                        <!-- Scan Info -->
                        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 bg-gray-50 rounded-xl p-4 text-xs">
                            <div>
                                <div class="text-gray-400 mb-1">报告编号</div>
                                <div class="font-mono font-bold text-gray-700">{{ report.reportId }}</div>
                            </div>
                            <div>
                                <div class="text-gray-400 mb-1">检测时间</div>
                                <div class="font-bold text-gray-700">{{ report.inspectDate }}</div>
                            </div>
                            <div>
                                <div class="text-gray-400 mb-1">状态</div>
                                <div class="font-bold text-gray-700">{{ report.statusDisplay }}</div>
                            </div>
                            <div>
                                <div class="text-gray-400 mb-1">质检员</div>
                                <div class="font-bold text-gray-700 flex items-center gap-1">
                                    <div class="i-lucide-award text-[#4a8b6e]"></div>
                                    官方认证
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 2. Inspection Summary Grid -->
            <!-- Simplified summary because backend doesn't split functionality/cosmetic explicitly in DTO -->
            <div class="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
                <div class="flex items-center gap-2 mb-4">
                    <div class="w-8 h-8 rounded-full bg-blue-50 text-blue-600 flex items-center justify-center">
                        <div class="i-lucide-clipboard-check w-4 h-4"></div>
                    </div>
                    <h3 class="font-bold text-lg">质检摘要</h3>
                </div>
                <div class="text-gray-600 text-sm leading-relaxed p-2 bg-gray-50 rounded-lg" v-if="report.summary">
                    {{ report.summary }}
                </div>
                <div class="text-gray-400 text-sm p-2" v-else>
                    暂无详细摘要
                </div>
            </div>

            <!-- 3. Detailed Checklist (Accordion style simplified) -->
            <div class="bg-white rounded-3xl p-6 shadow-sm border border-gray-100">
                <h3 class="font-bold text-lg mb-6 flex items-center gap-2">
                    <div class="w-1 h-6 bg-[#4a8b6e] rounded-full"></div>
                    详细检测项
                </h3>

                <div v-if="checklist.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    <div class="space-y-3 col-span-full">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                            <div v-for="item in checklist" :key="item.id"
                                class="flex justify-between items-center text-sm p-2 hover:bg-gray-50 rounded transition-colors border-b border-gray-50 last:border-0">
                                <div class="flex flex-col">
                                    <span class="text-gray-700 font-medium">{{ item.itemName }}</span>
                                    <span class="text-xs text-gray-400" v-if="item.itemDescription">{{
                                        item.itemDescription }}</span>
                                </div>
                                <div v-if="item.status === 'PASSED'"
                                    class="text-[#4a8b6e] bg-[#4a8b6e]/5 px-2 py-1 rounded text-xs flex items-center gap-1 flex-shrink-0">
                                    <div class="i-lucide-check w-3 h-3"></div> 正常
                                </div>
                                <div v-else
                                    class="text-red-500 bg-red-50 px-2 py-1 rounded text-xs flex items-center gap-1 flex-shrink-0">
                                    <div class="i-lucide-alert-circle w-3 h-3"></div> {{ item.notes || '异常' }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div v-else class="text-center text-gray-400 py-10">
                    暂无检测项目数据
                </div>
            </div>

            <!-- Footer Statement -->
            <div class="text-center text-xs text-gray-400 py-8 leading-relaxed">
                <p>当前报告基于后端验货流接口的实际字段渲染，摘要和检测项来自当前验货记录。</p>
                <p>如后续补充更多结构化字段，可继续扩展报告编号、检测员信息与 PDF 元数据展示。</p>
            </div>

        </main>
    </div>
</template>

<script setup>
import { getMyInspections, getOrderInspectionReport } from '@/api/inspection';
import request from '@/utils/request';
import { showFailToast } from 'vant';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const report = ref({});
const checklist = ref([]);
const loading = ref(true);
const reportUnavailable = ref(false);
const unavailableMessage = ref('检测流程未完成，报告内容暂不可查看');

const mapInspectionToReport = (found) => {
    report.value = {
        id: found.inspectionId,
        grade: found.grade || 'A',
        title: found.productTitle,
        image: found.productImage,
        category: found.categoryName,
        reportId: `R-${new Date(found.createdAt).getFullYear()}${String(new Date(found.createdAt).getMonth() + 1).padStart(2, '0')}-${String(found.inspectionId).padStart(3, '0')}`,
        inspectDate: new Date(found.updatedAt || found.createdAt).toLocaleString(),
        status: found.status,
        statusDisplay: toStatusDisplay(found.status),
        summary: sanitizeSummary(found.resultSummary) || '暂无详细摘要'
    };
    checklist.value = found.items || [];
};

const handleUnavailableReport = (status, type) => {
    if (status === 'PENDING' || status === 'IN_PROGRESS') {
        reportUnavailable.value = true;
        unavailableMessage.value = type === 'order'
            ? '本单关联的验货流程尚未完成，报告内容暂不可查看'
            : '检测流程未完成，报告内容暂不可查看';
        return true;
    }
    return false;
};

const loadReport = async () => {
    const id = route.params.id;
    const type = route.query.type === 'order' ? 'order' : 'inspection';
    if (!id) {
        showFailToast('报告ID不存在');
        loading.value = false;
        return;
    }

    reportUnavailable.value = false;
    report.value = {};
    checklist.value = [];

    try {
        if (type === 'order') {
            const found = await getOrderInspectionReport(id);
            if (found && !handleUnavailableReport(found.status, type)) {
                mapInspectionToReport(found);
            }
            return;
        }

        const res = await getMyInspections();
        if (res) {
            const found = res.find(r => String(r.inspectionId) === String(id));
            if (!found) {
                return;
            }

            if (!handleUnavailableReport(found.status, type)) {
                mapInspectionToReport(found);
            }
        }
    } catch (e) {
        if (e?.response?.status === 404) {
            return;
        }
        console.error('Fetch inspection detail failed', e);
        showFailToast('加载报告失败');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadReport();
});

const getGradeColor = (grade) => {
    switch (grade) {
        case 'S': return 'bg-gradient-to-b from-yellow-400 to-yellow-600';
        case 'A': return 'bg-gradient-to-b from-blue-400 to-blue-600';
        case 'B': return 'bg-gradient-to-b from-orange-400 to-orange-600';
        case 'X': return 'bg-gray-500';
        default: return 'bg-gray-400';
    }
};

const toStatusDisplay = (status) => {
    if (status === 'COMPLETED') return '已完成';
    if (status === 'PENDING') return '待检测';
    if (status === 'IN_PROGRESS') return '检测中';
    if (status === 'FAILED') return '未通过';
    return status || '未知';
};

const onImageError = (event) => {
    event.target.src = 'https://images.unsplash.com/photo-1512054502232-10a0a035d672?auto=format&fit=crop&q=80&w=300';
};

const sanitizeSummary = (summary) => {
    if (!summary) return '';
    if (summary.includes('Could not initialize proxy') || summary.includes('no session')) {
        return '验货流程中断，系统已记录异常。未通过原因：检测流程异常，请联系客服复检';
    }
    return summary;
};

const handleDownload = async () => {
    try {
        if (!report.value.id) return;

        showFailToast({ // Using toast as loading indicator briefly
            message: '生成中...',
            duration: 500, // short duration or use loading toast if available
            type: 'loading'
        });

        const res = await request.get(`/inspections/${report.value.id}/pdf`, {
            responseType: 'blob'
        });

        // Interceptor returns response.data (the Blob) directly
        const blob = new Blob([res], { type: 'application/pdf' });

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `inspection-report-${report.value.id}.pdf`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) {
        if (e.response && e.response.data instanceof Blob) {
            const reader = new FileReader();
            reader.onload = () => {
                try {
                    // 尝试把 Blob 转回 JSON 文本
                    const errorMsg = JSON.parse(reader.result);
                    console.error('后端报错详情:', errorMsg);
                    showFailToast('导出失败: ' + (errorMsg.message || '系统内部错误'));
                } catch (e) {
                     // 可能是纯文本错误信息
                    console.error('后端返回错误文本:', reader.result);
                    showFailToast('导出失败: ' + reader.result);
                }
            };
            // 读取 Blob 内容
            reader.readAsText(e.response.data);
        } else {
            console.error('下载失败', e);
        }
    }
};
</script>
