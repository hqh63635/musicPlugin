<script setup>
import { mobileNavItems } from '@/constants/navigation.js';
import { useRoute } from 'vue-router';

const route = useRoute();

const isActive = path => route.path === path || route.path.startsWith(`${path}/`);
</script>

<template>
  <nav class="mobile-nav">
    <router-link
      v-for="item in mobileNavItems"
      :key="item.key"
      :to="item.path"
      class="mobile-nav-item"
      :class="{ active: isActive(item.path) }"
    >
      <component :is="item.icon" class="mobile-nav-icon" />
      <span class="mobile-nav-text">{{ $t(item.text) }}</span>
    </router-link>
  </nav>
</template>

<style scoped>
.mobile-nav {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  align-items: center;
  height: 58px;
  padding: 4px 8px calc(4px + var(--app-safe-bottom));
  background: var(--theme-bg-primary);
  border-top: 1px solid var(--theme-border-primary);
}

.mobile-nav-item {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--theme-text-secondary);
  transition: color 0.2s ease;
}

.mobile-nav-item.active {
  color: var(--theme-accent-primary);
}

.mobile-nav-icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.mobile-nav-text {
  max-width: 100%;
  font-size: 11px;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
