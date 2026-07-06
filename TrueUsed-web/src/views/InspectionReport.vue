<template>
    <div class="min-h-screen bg-transparent pb-10">
        <div class="mx-auto max-w-[1480px] px-8 py-6">
            <section
                class="relative overflow-hidden rounded-[32px] border border-white/70 bg-gradient-to-br from-[#0f1f2a] via-[#173544] to-[#235263] px-8 py-8 text-white shadow-[0_28px_80px_rgba(15,23,42,0.18)]">
                <div class="absolute right-0 top-0 h-72 w-72 rounded-full bg-white/10 blur-3xl"></div>

                <div class="relative z-10 flex items-end justify-between gap-8">
                    <div>
                        <div
                            class="inline-flex items-center gap-2 rounded-full border border-sky-300/20 bg-sky-200/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-sky-100">
                            <div class="i-lucide-file-badge-2 text-sm"></div>
                            Inspection Archive
                        </div>
                        <h1 class="mt-4 text-4xl font-black tracking-tight">验货报告档案</h1>
                        <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-100/72">
                            这页现在直接挂在主 navbar 下，不再单独跳到一套独立页面。当前列表已经能串起浏览与筛选流程，但部分说明字段仍是演示占位，后续可逐步替换成真实验货数据。
                        </p>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div class="rounded-3xl border border-white/10 bg-white/10 px-5 py-4 backdrop-blur">
                            <div class="text-xs uppercase tracking-[0.18em] text-slate-200/60">报告总数</div>
                            <div class="mt-2 text-3xl font-black">{{ totalCount }}</div>
                        </div>
                        <div class="rounded-3xl border border-white/10 bg-white/10 px-5 py-4 backdrop-blur">
                            <div class="text-xs uppercase tracking-[0.18em] text-slate-200/60">当前筛选</div>
                            <div class="mt-2 text-3xl font-black">{{ filteredReports.length }}</div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-6 grid grid-cols-4 gap-5">
                <div v-for="card in summaryCards" :key="card.label"
                    class="rounded-[28px] border border-white/70 bg-white/88 p-5 shadow-[0_18px_38px_rgba(15,23,42,0.05)] backdrop-blur">
                    <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">{{ card.label }}</div>
                    <div class="mt-3 text-3xl font-black tracking-tight text-slate-950">{{ card.value }}</div>
                    <div class="mt-2 text-sm text-slate-500">{{ card.note }}</div>
                </div>
            </section>

            <section class="mt-6">
                <div
                    class="rounded-[32px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                    <div class="flex items-end justify-between gap-5">
                        <div>
                            <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">Report Desk</div>
                            <h2 class="mt-2 text-2xl font-black text-slate-950">报告列表</h2>
                            <p class="mt-2 text-sm text-slate-500">按状态和编号快速筛选报告，不离开主导航壳层。</p>
                        </div>

                        <div class="flex items-center gap-3">
                            <div class="w-[360px]">
                                <SearchBar v-model="searchQuery" placeholder="输入商品名称 / 报告编号"
                                    :show-shortcut="false" :show-submit="false" />
                            </div>

                            <button type="button"
                                class="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-600 transition-colors hover:border-emerald-200 hover:text-emerald-700">
                                <div class="i-lucide-filter w-4 h-4"></div>
                                筛选
                            </button>
                        </div>
                    </div>

                    <div class="mt-6 flex gap-2 border-b border-slate-100 pb-2">
                        <button v-for="tab in tabs" :key="tab" type="button"
                            class="rounded-full px-4 py-2 text-sm font-semibold transition-colors"
                            :class="activeTab === tab ? 'bg-slate-900 text-white' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'"
                            @click="activeTab = tab">
                            {{ tab }}
                        </button>
                    </div>

                    <div class="mt-6 space-y-4">
                        <div v-if="filteredReports.length === 0"
                            class="flex min-h-[280px] flex-col items-center justify-center rounded-[28px] bg-slate-50 text-center text-slate-400">
                            <div class="mb-4 rounded-full bg-white p-5 shadow-sm">
                                <div class="i-lucide-file-x w-7 h-7"></div>
                            </div>
                            <p class="text-base font-semibold text-slate-700">暂无相关报告</p>
                            <p class="mt-2 text-sm">换个关键词或筛选条件再试。</p>
                        </div>

                        <div v-for="report in filteredReports" :key="report.id"
                            class="group grid grid-cols-[260px_minmax(0,1fr)_180px] rounded-[28px] border border-slate-200 bg-white p-2 shadow-sm transition-all hover:border-emerald-200 hover:shadow-[0_18px_34px_rgba(15,23,42,0.08)]">
                            <div class="relative h-[220px] overflow-hidden rounded-[22px] bg-slate-100">
                                <img :src="report.image"
                                    class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />

                                <div class="absolute left-4 top-4">
                                    <div
                                        :class="['flex h-14 w-12 flex-col items-center justify-center rounded-xl text-white shadow-lg', getGradeColor(report.grade)]">
                                        <span class="text-lg font-black leading-none">{{ report.grade }}</span>
                                        <span class="mt-1 text-[9px] opacity-80">级</span>
                                    </div>
                                </div>

                                <div
                                    class="absolute inset-x-0 bottom-0 flex items-center justify-between bg-black/60 px-4 py-3 text-xs text-white backdrop-blur-sm">
                                    <span>{{ report.inspectDate }}</span>
                                    <span class="font-mono opacity-85">{{ report.reportId }}</span>
                                </div>
                            </div>

                            <div class="flex min-w-0 flex-col justify-between px-6 py-4">
                                <div>
                                    <div class="flex items-start justify-between gap-4">
                                        <div class="min-w-0">
                                            <h3 class="truncate text-xl font-black text-slate-950">{{ report.title }}</h3>
                                            <div class="mt-2 text-sm text-slate-400">{{ report.category }} · {{ report.spec }}</div>
                                        </div>

                                        <span v-if="report.status === '通过'"
                                            class="inline-flex shrink-0 items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-700">
                                            <div class="i-lucide-check-circle-2 w-3 h-3"></div>
                                            验货通过
                                        </span>
                                        <span v-else
                                            class="inline-flex shrink-0 items-center gap-1 rounded-full bg-red-50 px-3 py-1 text-xs font-bold text-red-600">
                                            <div class="i-lucide-x-circle w-3 h-3"></div>
                                            验货驳回
                                        </span>
                                    </div>

                                    <div class="mt-5">
                                        <div class="text-xs font-bold uppercase tracking-[0.18em] text-slate-400">关键检测项</div>
                                        <div class="mt-3 flex flex-wrap gap-2">
                                            <span v-for="item in report.highlights" :key="item.label"
                                                :class="['inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs', item.pass ? 'border-slate-200 bg-slate-50 text-slate-600' : 'border-orange-200 bg-orange-50 text-orange-700']">
                                                <div :class="[item.pass ? 'i-lucide-check' : 'i-lucide-alert-circle', 'w-3 h-3']"></div>
                                                {{ item.label }}
                                                <span v-if="item.value" class="font-bold">{{ item.value }}</span>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <div class="mt-6 flex items-center gap-2 border-t border-slate-100 pt-4 text-xs text-slate-400">
                                    <div class="i-lucide-user-check w-3.5 h-3.5"></div>
                                    <span>质检工程师：{{ report.inspector }}</span>
                                    <div class="h-3 w-px bg-slate-200"></div>
                                    <span>{{ report.location }}验货中心</span>
                                </div>
                            </div>

                            <div class="flex flex-col justify-center gap-3 border-l border-slate-100 px-5">
                                <button type="button"
                                    class="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-900 px-4 py-3 text-sm font-bold text-white transition-colors hover:bg-slate-800"
                                    @click="router.push({ name: 'InspectionDetail', params: { id: report.id } })">
                                    <div class="i-lucide-eye w-4 h-4"></div>
                                    查看详情
                                </button>
                                <button type="button"
                                    class="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-600 transition-colors hover:border-emerald-200 hover:text-emerald-700">
                                    <div class="i-lucide-download w-4 h-4"></div>
                                    下载 PDF
                                </button>
                                <button type="button"
                                    class="text-xs font-medium text-slate-400 transition-colors hover:text-slate-700">
                                    分享报告
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup>
import { getMyInspections } from '@/api/inspection';
import SearchBar from '@/components/SearchBar.vue';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchQuery = ref('');
const activeTab = ref('全部');
const tabs = ['全部', '已通过', '未通过', '30天内'];
const reports = ref([]);

const totalCount = computed(() => reports.value.length);
const passedCount = computed(() => reports.value.filter(report => report.status === '通过').length);
const failedCount = computed(() => reports.value.filter(report => report.status === '驳回').length);
const recentCount = computed(() => {
    const now = Date.now();
    const thirtyDays = 30 * 24 * 60 * 60 * 1000;

    return reports.value.filter(report => {
        return now - report.timestamp <= thirtyDays;
    }).length;
});

const summaryCards = computed(() => {
    return [
        {
            label: '报告总数',
            value: `${totalCount.value}`,
            note: '当前账号可见的验货档案'
        },
        {
            label: '通过报告',
            value: `${passedCount.value}`,
            note: '已完成并通过平台验货'
        },
        {
            label: '驳回报告',
            value: `${failedCount.value}`,
            note: '检测未通过，建议回看详情'
        },
        {
            label: '30 天内',
            value: `${recentCount.value}`,
            note: '最近新生成的报告数量'
        }
    ];
});

const filteredReports = computed(() => {
    let result = reports.value;

    if (activeTab.value === '已通过') {
        result = result.filter(report => report.status === '通过');
    } else if (activeTab.value === '未通过') {
        result = result.filter(report => report.status === '驳回');
    } else if (activeTab.value === '30天内') {
        const now = Date.now();
        const thirtyDays = 30 * 24 * 60 * 60 * 1000;
        result = result.filter(report => now - report.timestamp <= thirtyDays);
    }

    if (searchQuery.value.trim()) {
        const keyword = searchQuery.value.trim().toLowerCase();
        result = result.filter(report => {
            return report.title.toLowerCase().includes(keyword) || report.reportId.toLowerCase().includes(keyword);
        });
    }

    return result;
});

async function fetchReports() {
    try {
        const res = await getMyInspections();
        reports.value = (res || [])
            .filter(item => item.status === 'COMPLETED' || item.status === 'FAILED')
            .map(item => {
                const createdAt = new Date(item.createdAt).getTime();
                const updatedAt = new Date(item.updatedAt || item.createdAt).getTime();

                return {
                    id: item.inspectionId,
                    reportId: item.reportNo || `R-${new Date(item.createdAt).getFullYear()}${String(new Date(item.createdAt).getMonth() + 1).padStart(2, '0')}-${String(item.inspectionId).padStart(3, '0')}`,
                    title: item.productTitle || '未知商品',
                    image: item.productImage || 'https://images.unsplash.com/photo-1512054502232-10a0a035d672?auto=format&fit=crop&q=80&w=800',
                    grade: item.grade || 'A',
                    status: item.status === 'COMPLETED' ? '通过' : '驳回',
                    category: item.categoryName || '通用',
                    spec: item.productSpec || '待接商品规格',
                    inspectDate: new Date(updatedAt).toLocaleString(),
                    inspector: item.inspectorName || item.inspectorId || '待接质检员',
                    location: item.inspectionCenterName || item.centerCity || '待接验货中心',
                    timestamp: updatedAt || createdAt,
                    highlights: (item.items || []).slice(0, 4).map(detail => ({
                        label: detail.itemName,
                        pass: detail.status === 'PASSED',
                        value: detail.notes
                    }))
                };
            });
    } catch (error) {
        console.error('Failed to fetch inspections', error);
    }
}

function getGradeColor(grade) {
    switch (grade) {
        case 'S':
            return 'bg-gradient-to-b from-yellow-400 to-yellow-600';
        case 'A':
            return 'bg-gradient-to-b from-blue-400 to-blue-600';
        case 'B':
            return 'bg-gradient-to-b from-orange-400 to-orange-600';
        case 'X':
            return 'bg-gray-500';
        default:
            return 'bg-gray-400';
    }
}

onMounted(() => {
    fetchReports();
});
</script>
