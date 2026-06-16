<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h3>控制台概览</h3>
        <p class="subtitle">系统核心数据与运营分析</p>
      </div>
    </div>

    <el-row :gutter="14" class="stat-row">
      <el-col :span="4" v-for="item in cards" :key="item.label">
        <div
          class="stat-card"
          :class="{ clickable: item.path }"
          :style="{ '--accent': item.color }"
          @click="item.path && $router.push(item.path)"
        >
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="chart-row">
      <el-col :span="12">
        <div class="chart-panel">
          <div class="chart-header">
            <h4>近7日上传趋势</h4>
          </div>
          <div ref="trendRef" class="chart-container" />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-panel">
          <div class="chart-header">
            <h4>审核结果分布</h4>
          </div>
          <div ref="auditRef" class="chart-container" />
        </div>
      </el-col>
    </el-row>

    <div class="chart-panel" style="margin-top: 14px">
      <div class="chart-header">
        <h4>上传量 Top 用户</h4>
      </div>
      <div ref="topRef" class="chart-container" />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardCharts, getDashboardOverview } from '@/api/admin'

const loading = ref(false)
const data = ref({})
const charts = ref({})
const trendRef = ref(null)
const auditRef = ref(null)
const topRef = ref(null)
let trendChart = null
let auditChart = null
let topChart = null

const cards = computed(() => [
  { label: '普通用户', value: data.value.userCount ?? 0, color: '#009688' },
  { label: '管理员', value: data.value.adminCount ?? 0, color: '#42a5f5' },
  { label: '待审核文件', value: data.value.pendingFileCount ?? 0, color: '#ffa726', path: '/admin/file' },
  { label: '待回复反馈', value: data.value.pendingFeedbackCount ?? 0, color: '#ef5350', path: '/admin/feedback' },
  { label: '文件总数', value: data.value.totalFileCount ?? 0, color: '#7e57c2' },
  { label: '已通过文件', value: data.value.approvedFileCount ?? 0, color: '#26a69a' }
])

function renderTrendChart() {
  if (!trendRef.value) return
  if (!trendChart) trendChart = echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: charts.value.uploadTrendDates || [] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '上传数',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#009688' },
      data: charts.value.uploadTrendCounts || []
    }]
  })
}

function renderAuditChart() {
  if (!auditRef.value) return
  if (!auditChart) auditChart = echarts.init(auditRef.value)
  auditChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: [
        { name: '已通过', value: charts.value.approvedCount || 0, itemStyle: { color: '#26a69a' } },
        { name: '已拒绝', value: charts.value.rejectedCount || 0, itemStyle: { color: '#ef5350' } },
        { name: '待审核', value: charts.value.pendingCount || 0, itemStyle: { color: '#ffa726' } }
      ]
    }]
  })
}

function renderTopChart() {
  if (!topRef.value) return
  if (!topChart) topChart = echarts.init(topRef.value)
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: charts.value.topUploaderNames || [] },
    series: [{
      type: 'bar',
      data: charts.value.topUploaderCounts || [],
      itemStyle: { color: '#42a5f5', borderRadius: [0, 4, 4, 0] }
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  auditChart?.resize()
  topChart?.resize()
}

async function loadData() {
  loading.value = true
  try {
    const [overviewRes, chartsRes] = await Promise.all([getDashboardOverview(), getDashboardCharts()])
    data.value = overviewRes.data
    charts.value = chartsRes.data
    await nextTick()
    renderTrendChart()
    renderAuditChart()
    renderTopChart()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  auditChart?.dispose()
  topChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  background: var(--card-bg);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  min-height: calc(100vh - 148px);
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.subtitle {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-row {
  margin: 20px 0;
}

.stat-card {
  padding: 18px 12px;
  text-align: center;
  border-radius: var(--radius);
  background: #fff;
  border: 1px solid var(--border-color);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--accent, var(--primary));
  line-height: 1;
}

.stat-label {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.chart-row {
  margin-top: 4px;
}

.chart-panel {
  padding: 20px;
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
  background: #fafbfc;
}

.chart-header h4 {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>
