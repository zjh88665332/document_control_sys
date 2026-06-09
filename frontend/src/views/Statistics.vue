<template>
  <div class="statistics-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h3>文件类型统计</h3>
        <p class="subtitle">已通过审核文件的类型分布概览</p>
      </div>
      <div class="total-badge">
        <span class="total-label">文件总数</span>
        <span class="total-value">{{ totalCount }}</span>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statItems" :key="item.key">
        <div class="stat-card" :style="{ '--accent': item.color }">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[item.key] ?? 0 }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-panel">
      <div class="chart-header">
        <h4>类型数量趋势</h4>
        <span class="chart-tip">各类型文件数量对比折线图</span>
      </div>
      <div ref="chartRef" class="chart-container" />
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { getFileTypeStatistics } from '@/api'

const loading = ref(false)
const stats = ref({ document: 0, image: 0, video: 0, compress: 0 })
const chartRef = ref(null)
let chartInstance = null

const statItems = [
  { key: 'document', label: '文档', color: '#009688', icon: '文' },
  { key: 'image', label: '图片', color: '#42a5f5', icon: '图' },
  { key: 'video', label: '视频', color: '#ffa726', icon: '视' },
  { key: 'compress', label: '压缩包', color: '#ab47bc', icon: '压' }
]

const totalCount = computed(() =>
  statItems.reduce((sum, item) => sum + (stats.value[item.key] ?? 0), 0)
)

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const labels = statItems.map((item) => item.label)
  const values = statItems.map((item) => stats.value[item.key] ?? 0)

  chartInstance.setOption({
    color: statItems.map((item) => item.color),
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' }
    },
    grid: { left: 48, right: 24, top: 36, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2f6', type: 'dashed' } },
      axisLabel: { color: '#909399' }
    },
    series: [
      {
        name: '文件数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 10,
        lineStyle: { width: 3, color: '#009688' },
        itemStyle: {
          color: '#009688',
          borderColor: '#fff',
          borderWidth: 2
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,150,136,0.35)' },
            { offset: 1, color: 'rgba(0,150,136,0.04)' }
          ])
        },
        data: values
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
    const res = await getFileTypeStatistics()
    stats.value = res.data
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

watch(stats, () => {
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
.statistics-page {
  background: var(--card-bg);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  padding: 24px;
  min-height: calc(100vh - 148px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.subtitle {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.total-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 10px 18px;
  border-radius: var(--radius);
  background: var(--primary);
  color: #fff;
}

.total-label {
  font-size: 12px;
  opacity: 0.9;
}

.total-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-radius: var(--radius);
  background: #fff;
  border: 1px solid var(--border-color);
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, white);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.chart-panel {
  margin-top: 8px;
  padding: 20px 20px 8px;
  border-radius: var(--radius);
  background: #fafbfc;
  border: 1px solid var(--border-color);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chart-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-tip {
  font-size: 12px;
  color: #909399;
}

.chart-container {
  width: 100%;
  height: 360px;
}
</style>
