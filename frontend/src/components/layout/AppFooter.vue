<template>
  <footer class="app-footer">
    <div class="app-footer__inner">
      <div class="footer-brand">
        <div class="footer-logo">🦆</div>
        <div class="footer-copy">
          <h4>面试鸭刷题神</h4>
          <p>和优秀的伙伴一起，一题一进步。</p>
        </div>
      </div>

      <div class="footer-links">
        <div class="footer-column">
          <span class="column-title">产品</span>
          <a href="#">题库广场</a>
          <a href="#">刷题路线</a>
          <a href="#">成长社区</a>
        </div>
        <div class="footer-column">
          <span class="column-title">支持</span>
          <a href="#">帮助中心</a>
          <a href="#">常见问题</a>
          <a href="#">更新日志</a>
        </div>
        <div class="footer-column">
          <span class="column-title">联系</span>
          <a href="mailto:hi@mianshiya.com">hi@mianshiya.com</a>
          <a href="#">加入我们</a>
        </div>
      </div>
    </div>
    <div class="app-footer__meta">
      <span>© {{ new Date().getFullYear() }} 面试鸭刷题神. Crafted with care in China.</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  margin-top: auto;
  background: radial-gradient(circle at top, rgba(255, 122, 69, 0.22), rgba(15, 23, 42, 0.92));
  color: rgba(255, 255, 255, 0.88);
  padding: 52px 0 28px;
  position: relative;
  overflow: hidden;
}

.app-footer::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(14px);
}

.app-footer__inner {
  position: relative;
  z-index: 1;
  width: min(1180px, 92vw);
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 48px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 18px;
  max-width: 320px;
}

.footer-logo {
  width: 54px;
  height: 54px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 30px;
  background: rgba(15, 23, 42, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.22);
  box-shadow: 0 16px 24px rgba(0, 0, 0, 0.22);
}

.footer-copy h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
}

.footer-copy p {
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 32px;
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
}

.column-title {
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.footer-column a {
  color: rgba(255, 255, 255, 0.78);
  transition: color 0.2s ease, transform 0.2s ease;
}

.footer-column a:hover {
  color: #fff;
  transform: translateX(4px);
}

.app-footer__meta {
  position: relative;
  z-index: 1;
  margin-top: 38px;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
}

@media (max-width: 768px) {
  .app-footer {
    padding: 40px 0 24px;
  }

  .app-footer__inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 32px;
  }

  .footer-links {
    width: 100%;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>
