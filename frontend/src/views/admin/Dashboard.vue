<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h3>控制台概览</h3>
        <p class="subtitle">系统核心数据一览</p>
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

    <div class="chart-panel">
      <div class="chart-header">
        <h4>数据柱形图</h4>
        <span class="chart-tip">各指标数量对比</span>
      </div>
      <div ref="chartRef" class="chart-container" />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { getDashboardOverview } from '@/api/admin'

const loading = ref(false)
const data = ref({})
const chartRef = ref(null)
let chartInstance = null

const cards = computed(() => [
  { label: '普通用户', value: data.value.userCount ?? 0, color: '#009688' },
  { label: '管理员', value: data.value.adminCount ?? 0, color: '#42a5f5' },
  { label: '待审核文件', value: data.value.pendingFileCount ?? 0, color: '#ffa726', path: '/admin/file' },
  { label: '待回复反馈', value: data.value.pendingFeedbackCount ?? 0, color: '#ef5350', path: '/admin/feedback' },
  { label: '文件总数', value: data.value.totalFileCount ?? 0, color: '#7e57c2' },
  { label: '已通过文件', value: data.value.approvedFileCount ?? 0, color: '#26a69a' }
])

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const labels = cards.value.map((item) => item.label)
  const values = cards.value.map((item) => item.value)
  const colors = cards.value.map((item) => item.color)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' }
    },
    grid: { left: 48, right: 24, top: 24, bottom: 40 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266', interval: 0, rotate: 0, fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2f6', type: 'dashed' } },
      axisLabel: { color: '#909399' }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '42%',
        data: values.map((val, idx) => ({
          value: val,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: colors[idx] },
              { offset: 1, color: `${colors[idx]}99` }
            ]),
            borderRadius: [6, 6, 0, 0]
          }
        }))
      }
    ]
  })
}

function handleResize() {
  chartInstance?.resize()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDashboardOverview()
    data.value = res.data
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

watch(cards, () => {
  nextTick(() => renderChart())
}, { deep: true })

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
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
  border-color: color-mix(in srgb, var(--accent) 40%, white);
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

.chart-panel {
  padding: 20px;
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
  background: #fafbfc;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chart-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.chart-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.chart-container {
  width: 100%;
  height: 340px;
}
</style>
