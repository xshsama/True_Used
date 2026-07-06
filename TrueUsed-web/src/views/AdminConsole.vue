<template>
  <div class="admin-console">
    <header class="admin-topbar">
      <div class="admin-topbar__brand">
        <div class="admin-topbar__mark">T</div>
        <div>
          <p class="admin-topbar__eyebrow">TRUEUSED PLATFORM OPS</p>
          <h1 class="admin-topbar__title">Admin Console</h1>
        </div>
      </div>
      <div class="admin-topbar__actions">
        <button class="admin-button admin-button--ghost" type="button" @click="goHome">
          返回前台
        </button>
        <button class="admin-button admin-button--primary" type="button" @click="reloadAll" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新面板' }}
        </button>
      </div>
    </header>

    <main class="admin-layout">
      <section class="admin-hero">
        <div class="admin-hero__copy">
          <p class="admin-section-eyebrow">LINEAR ALPHA CONTROL SURFACE</p>
          <h2 class="admin-hero__headline">管理端只给管理员，操作面和交易主站彻底分层。</h2>
          <p class="admin-hero__description">
            这版先覆盖平台最关键的后台动作：用户状态控制、提现审核、运行态总览。保持单项目接入，不新开站，不复制登录体系。
          </p>
        </div>

        <div class="admin-shot">
          <div class="admin-shot__chrome">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <div class="admin-shot__body">
            <div class="admin-shot__column">
              <span class="admin-shot__label">OPS STATUS</span>
              <strong>{{ summary.activeUsers }}</strong>
              <small>active accounts</small>
            </div>
            <div class="admin-shot__column">
              <span class="admin-shot__label">RISK QUEUE</span>
              <strong>{{ summary.pendingWithdrawals }}</strong>
              <small>pending withdrawals</small>
            </div>
            <div class="admin-shot__column">
              <span class="admin-shot__label">ADMIN SEATS</span>
              <strong>{{ summary.adminUsers }}</strong>
              <small>privileged operators</small>
            </div>
          </div>
        </div>
      </section>

      <section class="admin-metrics">
        <article class="metric-card">
          <span class="metric-card__eyebrow">USER BASE</span>
          <strong class="metric-card__value">{{ summary.totalUsers }}</strong>
          <span class="metric-card__meta">全量平台注册用户</span>
        </article>
        <article class="metric-card">
          <span class="metric-card__eyebrow">ADMIN</span>
          <strong class="metric-card__value">{{ summary.adminUsers }}</strong>
          <span class="metric-card__meta">持有 `ROLE_ADMIN` 的账号</span>
        </article>
        <article class="metric-card">
          <span class="metric-card__eyebrow">DISABLED</span>
          <strong class="metric-card__value">{{ summary.disabledUsers }}</strong>
          <span class="metric-card__meta">已禁用用户，待观察或封禁中</span>
        </article>
        <article class="metric-card metric-card--accent">
          <span class="metric-card__eyebrow">WITHDRAWAL QUEUE</span>
          <strong class="metric-card__value">{{ summary.pendingWithdrawals }}</strong>
          <span class="metric-card__meta">待审核提现申请</span>
        </article>
      </section>

      <section class="admin-content">
        <article class="console-card console-card--wide">
          <div class="console-card__header">
            <div>
              <p class="admin-section-eyebrow">USER GOVERNANCE</p>
              <h3 class="console-card__title">用户管理</h3>
            </div>
            <div class="console-card__toolbar">
              <input
                v-model.trim="keyword"
                class="admin-input"
                type="text"
                placeholder="搜索用户名 / 昵称 / 邮箱"
              />
            </div>
          </div>

          <div class="console-table">
            <div class="console-table__head">
              <span>用户</span>
              <span>角色</span>
              <span>状态</span>
              <span>验证</span>
              <span>最近登录</span>
              <span>操作</span>
            </div>

            <div v-for="item in filteredUsers" :key="item.id" class="console-table__row">
              <div class="console-user">
                <div class="console-user__avatar">
                  {{ resolveUserInitial(item) }}
                </div>
                <div>
                  <p class="console-user__name">{{ item.nickname || item.username }}</p>
                  <p class="console-user__meta">{{ item.username }} · {{ item.email || '未填写邮箱' }}</p>
                </div>
              </div>

              <div class="console-role-list">
                <span v-for="role in item.roles || []" :key="role" class="admin-badge">
                  {{ role.replace('ROLE_', '') }}
                </span>
              </div>

              <div>
                <span class="admin-status" :class="item.status === 'ACTIVE' ? 'admin-status--ok' : 'admin-status--muted'">
                  {{ item.status }}
                </span>
              </div>

              <div class="console-meta">
                <span>{{ item.emailVerified ? '邮箱已验证' : '邮箱未验证' }}</span>
                <span>{{ item.phoneVerified ? '手机已验证' : '手机未验证' }}</span>
              </div>

              <div class="console-date">{{ formatDateTime(item.lastLoginAt) }}</div>

              <div class="console-actions">
                <button
                  v-if="item.status === 'ACTIVE'"
                  class="admin-button admin-button--ghost"
                  type="button"
                  @click="toggleUserStatus(item, 'disable')"
                >
                  禁用
                </button>
                <button
                  v-else
                  class="admin-button admin-button--primary"
                  type="button"
                  @click="toggleUserStatus(item, 'enable')"
                >
                  恢复
                </button>
              </div>
            </div>

            <div v-if="!filteredUsers.length" class="console-empty">
              没有匹配到用户。
            </div>
          </div>
        </article>

        <article class="console-card">
          <div class="console-card__header">
            <div>
              <p class="admin-section-eyebrow">FUNDS REVIEW</p>
              <h3 class="console-card__title">提现审核队列</h3>
            </div>
            <span class="admin-badge admin-badge--accent">{{ pendingWithdrawals.length }} pending</span>
          </div>

          <div class="queue-list">
            <div v-for="item in pendingWithdrawals" :key="item.id" class="queue-item">
              <div class="queue-item__main">
                <div>
                  <p class="queue-item__title">TX-{{ item.id }} · UID-{{ item.userId }}</p>
                  <p class="queue-item__meta">
                    {{ item.type }} · {{ item.status }} · {{ formatDateTime(item.createdAt) }}
                  </p>
                </div>
                <strong class="queue-item__amount">¥ {{ formatAmount(item.amount) }}</strong>
              </div>

              <p class="queue-item__remark">{{ item.remark || '无备注，默认按标准提现审核。' }}</p>

              <div class="queue-item__actions">
                <button class="admin-button admin-button--ghost" type="button" @click="reject(item)">
                  驳回
                </button>
                <button class="admin-button admin-button--primary" type="button" @click="approve(item)">
                  通过
                </button>
              </div>
            </div>

            <div v-if="!pendingWithdrawals.length" class="console-empty">
              当前没有待审核提现。
            </div>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import {
  approveWithdrawal,
  disableAdminUser,
  enableAdminUser,
  fetchAdminUsers,
  fetchPendingWithdrawals,
  rejectWithdrawal,
} from '@/api/admin'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  closeToast,
  showConfirmDialog,
  showFailToast,
  showLoadingToast,
  showSuccessToast,
} from 'vant'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const users = ref([])
const pendingWithdrawals = ref([])

const summary = computed(() => {
  const totalUsers = users.value.length
  const adminUsers = users.value.filter((item) =>
    Array.isArray(item.roles) ? item.roles.includes('ROLE_ADMIN') : false,
  ).length
  const activeUsers = users.value.filter((item) => item.status === 'ACTIVE').length
  const disabledUsers = users.value.filter((item) => item.status === 'DISABLED').length

  return {
    totalUsers,
    adminUsers,
    activeUsers,
    disabledUsers,
    pendingWithdrawals: pendingWithdrawals.value.length,
  }
})

const filteredUsers = computed(() => {
  const term = keyword.value.toLowerCase()
  if (!term) return users.value
  return users.value.filter((item) =>
    [item.username, item.nickname, item.email]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(term)),
  )
})

function formatDateTime(value) {
  if (!value) return '未记录'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '未记录'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function formatAmount(value) {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

function resolveUserInitial(item) {
  const label = item.nickname || item.username || 'U'
  return String(label).slice(0, 1).toUpperCase()
}

async function loadUsers() {
  users.value = await fetchAdminUsers()
}

async function loadWithdrawals() {
  const data = await fetchPendingWithdrawals()
  pendingWithdrawals.value = Array.isArray(data?.content) ? data.content : []
}

async function reloadAll() {
  loading.value = true
  showLoadingToast({ message: '刷新管理面板...', duration: 0, forbidClick: true, loadingType: 'spinner' })
  try {
    await Promise.all([loadUsers(), loadWithdrawals()])
  } catch (error) {
    const message = error?.message || '管理面板加载失败'
    showFailToast(message)
  } finally {
    loading.value = false
    closeToast()
  }
}

async function toggleUserStatus(item, action) {
  const isDisable = action === 'disable'
  try {
    await showConfirmDialog({
      title: isDisable ? '确认禁用用户' : '确认恢复用户',
      message: `${item.nickname || item.username} 将被${isDisable ? '禁用' : '恢复'}。`,
    })
    if (isDisable) {
      await disableAdminUser(item.id)
    } else {
      await enableAdminUser(item.id)
    }
    await loadUsers()
    showSuccessToast(isDisable ? '用户已禁用' : '用户已恢复')
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error?.message || '用户状态更新失败')
  }
}

async function approve(item) {
  try {
    await showConfirmDialog({
      title: '确认通过提现吗',
      message: `提现单 TX-${item.id} 将被审核通过。`,
    })
    await approveWithdrawal(item.id)
    await loadWithdrawals()
    showSuccessToast('提现已通过')
  } catch (error) {
    if (error === 'cancel') return
    showFailToast(error?.message || '提现审核失败')
  }
}

async function reject(item) {
  const reason = window.prompt('请输入驳回原因（可选）', '资料不完整，请补充后重新提交')
  if (reason === null) return
  try {
    await rejectWithdrawal(item.id, reason.trim())
    await loadWithdrawals()
    showSuccessToast('提现已驳回')
  } catch (error) {
    showFailToast(error?.message || '提现驳回失败')
  }
}

function goHome() {
  router.push('/home')
}

onMounted(() => {
  reloadAll()
})
</script>

<style scoped>
.admin-console {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.015), transparent 12%),
    #010102;
  color: #f7f8f8;
  padding: 24px 24px 64px;
  font-family:
    'SF Pro Display',
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

.admin-topbar,
.admin-hero,
.metric-card,
.console-card,
.admin-shot {
  border: 1px solid #23252a;
  background: #0f1011;
}

.admin-topbar {
  max-width: 1280px;
  margin: 0 auto 24px;
  min-height: 72px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
}

.admin-topbar__brand,
.admin-topbar__actions,
.console-card__header,
.console-user,
.queue-item__main,
.queue-item__actions {
  display: flex;
  align-items: center;
}

.admin-topbar__brand,
.console-user {
  gap: 14px;
}

.admin-topbar__actions,
.queue-item__actions {
  gap: 12px;
}

.admin-topbar__mark,
.console-user__avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #5e6ad2;
  color: #fff;
  font-weight: 700;
}

.admin-topbar__eyebrow,
.admin-section-eyebrow,
.metric-card__eyebrow,
.admin-shot__label {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: 0.4px;
  color: #8a8f98;
}

.admin-topbar__title {
  font-size: 22px;
  font-weight: 500;
  line-height: 1.25;
  letter-spacing: -0.4px;
}

.admin-layout {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  gap: 24px;
}

.admin-hero {
  border-radius: 16px;
  padding: 32px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.8fr);
  gap: 24px;
}

.admin-hero__headline {
  max-width: 720px;
  margin-top: 12px;
  font-size: clamp(40px, 5vw, 56px);
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -1.8px;
}

.admin-hero__description {
  max-width: 680px;
  margin-top: 16px;
  color: #d0d6e0;
  font-size: 18px;
  line-height: 1.5;
}

.admin-shot {
  border-radius: 16px;
  padding: 18px;
}

.admin-shot__chrome {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
}

.admin-shot__chrome span {
  width: 10px;
  height: 10px;
  border-radius: 9999px;
  background: #34343a;
}

.admin-shot__body {
  display: grid;
  gap: 12px;
}

.admin-shot__column {
  border: 1px solid #23252a;
  border-radius: 12px;
  background: #141516;
  padding: 18px;
}

.admin-shot__column strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.6px;
}

.admin-shot__column small,
.metric-card__meta,
.queue-item__meta,
.queue-item__remark,
.console-user__meta,
.console-date,
.console-meta {
  color: #8a8f98;
}

.admin-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  border-radius: 12px;
  padding: 24px;
}

.metric-card--accent {
  background: #141516;
  border-color: #34343a;
}

.metric-card__value {
  display: block;
  margin: 10px 0 8px;
  font-size: 40px;
  font-weight: 600;
  line-height: 1.15;
  letter-spacing: -1px;
}

.admin-content {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.65fr);
  gap: 16px;
}

.console-card {
  border-radius: 12px;
  padding: 24px;
}

.console-card__header {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.console-card__toolbar {
  width: min(320px, 100%);
}

.console-card__title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.6px;
}

.console-table {
  border-top: 1px solid #23252a;
}

.console-table__head,
.console-table__row {
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(140px, 1fr) 120px 140px 150px 92px;
  gap: 16px;
  align-items: center;
}

.console-table__head {
  padding: 14px 0;
  color: #8a8f98;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.console-table__row {
  min-height: 88px;
  padding: 16px 0;
  border-top: 1px solid #23252a;
}

.console-user__name,
.queue-item__title {
  font-size: 16px;
  font-weight: 500;
  color: #f7f8f8;
}

.console-user__meta,
.queue-item__meta,
.queue-item__remark,
.console-meta {
  margin-top: 4px;
  font-size: 14px;
  line-height: 1.5;
}

.console-meta {
  display: grid;
}

.console-role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.console-actions {
  display: flex;
  justify-content: flex-end;
}

.admin-input {
  width: 100%;
  min-height: 44px;
  border: 1px solid #23252a;
  border-radius: 8px;
  background: #0f1011;
  padding: 8px 12px;
  color: #f7f8f8;
  font-size: 16px;
}

.admin-input:focus {
  outline: 2px solid rgba(94, 106, 210, 0.5);
  border-color: #5e6ad2;
}

.admin-button {
  min-height: 40px;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.2;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.admin-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.admin-button--primary {
  background: #5e6ad2;
  color: #fff;
}

.admin-button--primary:hover:not(:disabled) {
  background: #828fff;
}

.admin-button--ghost {
  border: 1px solid #23252a;
  background: #0f1011;
  color: #f7f8f8;
}

.admin-button--ghost:hover:not(:disabled) {
  border-color: #34343a;
  background: #141516;
}

.admin-badge,
.admin-status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border-radius: 9999px;
  padding: 2px 8px;
  font-size: 12px;
  line-height: 1.4;
}

.admin-badge {
  background: #141516;
  color: #d0d6e0;
  border: 1px solid #23252a;
}

.admin-badge--accent {
  border-color: #34343a;
}

.admin-status--ok {
  background: rgba(39, 166, 68, 0.12);
  color: #9bdfab;
}

.admin-status--muted {
  background: #141516;
  color: #d0d6e0;
}

.queue-list {
  display: grid;
  gap: 12px;
}

.queue-item {
  border: 1px solid #23252a;
  border-radius: 12px;
  background: #141516;
  padding: 16px;
}

.queue-item__main {
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.queue-item__amount {
  font-size: 22px;
  font-weight: 500;
  line-height: 1.25;
  letter-spacing: -0.4px;
}

.queue-item__actions {
  justify-content: flex-end;
  margin-top: 16px;
}

.console-empty {
  padding: 24px 0 8px;
  color: #8a8f98;
  text-align: center;
}

@media (max-width: 1024px) {
  .admin-hero,
  .admin-content,
  .admin-metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-console {
    padding: 16px 16px 48px;
  }

  .admin-topbar,
  .admin-hero,
  .console-card {
    padding: 18px;
  }

  .admin-topbar,
  .admin-topbar__actions,
  .console-card__header {
    flex-direction: column;
    align-items: stretch;
  }

  .console-table__head {
    display: none;
  }

  .console-table__row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .console-actions {
    justify-content: flex-start;
  }
}
</style>
