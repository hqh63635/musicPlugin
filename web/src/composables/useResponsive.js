import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

export function useResponsive() {
  const viewportWidth = ref(typeof window === 'undefined' ? 1280 : window.innerWidth);

  const syncViewport = () => {
    viewportWidth.value = window.innerWidth;
  };

  onMounted(() => {
    syncViewport();
    window.addEventListener('resize', syncViewport, { passive: true });
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', syncViewport);
  });

  const isMobile = computed(() => viewportWidth.value <= 768);
  const isTablet = computed(() => viewportWidth.value <= 1024);

  return {
    viewportWidth,
    isMobile,
    isTablet,
  };
}
