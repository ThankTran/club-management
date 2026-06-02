import { useState, useRef, useEffect } from 'react';
import { galleryData } from '../../../../data/Public/content';
import styles from './Gallery.module.css';
import { useNavigate } from 'react-router-dom';

export default function Gallery() {
  const { title, description, images, cta } = galleryData;
  const looped = [...images, ...images]
  const navigate = useNavigate();

  const CARD_WIDTH = 280
  const GAP = 20
  const STEP = CARD_WIDTH + GAP
  const SPEED = 0.05
  const totalWidth = images.length * STEP

  const [offset, setOffset] = useState(0)
  const [paused, setPaused] = useState(false)
  const [selected, setSelected] = useState(null)
  const [inView, setInView] = useState(false)
  const rafRef = useRef(null)
  const lastTimeRef = useRef(null)
  const sectionRef = useRef(null)

  // IntersectionObserver
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => setInView(entry.isIntersecting),
      { threshold: 0.15 }
    )
    if (sectionRef.current) observer.observe(sectionRef.current)
    return () => observer.disconnect()
  }, [])

  // Auto scroll
  useEffect(() => {
    const animate = (time) => {
      if (!paused) {
        if (lastTimeRef.current !== null) {
          const delta = time - lastTimeRef.current
          setOffset(prev => {
            const next = prev + SPEED * delta
            return next >= totalWidth ? next - totalWidth : next
          })
        }
        lastTimeRef.current = time
      } else {
        lastTimeRef.current = null
      }
      rafRef.current = requestAnimationFrame(animate)
    }
    rafRef.current = requestAnimationFrame(animate)
    return () => cancelAnimationFrame(rafRef.current)
  }, [paused, totalWidth])

  // ESC close lightbox
  useEffect(() => {
    const onKey = (e) => { if (e.key === 'Escape') setSelected(null) }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  const prev = () => setOffset(o => {
    const next = o - STEP
    return next < 0 ? next + totalWidth : next
  })

  const next = () => setOffset(o => {
    const next = o + STEP
    return next >= totalWidth ? next - totalWidth : next
  })

  return (
    <section className={styles.section} id="gallery" ref={sectionRef}>
      {/* Floating shape */}
      <div className={styles.shape} />

      {/* Header */}
      <div className={`${styles.header} ${inView ? styles.headerVisible : ''}`}>
        <h2 className={styles.title}>{title}</h2>
        <p className={styles.desc}>{description}</p>
      </div>

      {/* Carousel */}
      <div className={`${styles.carouselRow} ${inView ? styles.carouselVisible : ''}`}>
        <button className={styles.btn} onClick={prev}>←</button>

        <div
          className={styles.viewport}
          onMouseEnter={() => setPaused(true)}
          onMouseLeave={() => setPaused(false)}
        >
          <div
            className={styles.track}
            style={{ transform: `translateX(-${offset}px)` }}
          >
            {looped.map((img, i) => (
              <div
                key={i}
                className={styles.card}
                onClick={() => setSelected(img)}
              >
                <div className={styles.cardImg}>
                  <img src={img.img} alt={img.label} className={styles.cardPhoto} />
                  <div className={styles.zoomHint}>🔍</div>
                </div>
                <div className={styles.caption}>
                  <span className={styles.captionDot} />
                  <span className={styles.captionText}>{img.label}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <button className={styles.btn} onClick={next}>→</button>
      </div>

      {/* CTA */}
      <div className={`${styles.ctaWrap} ${inView ? styles.ctaVisible : ''}`}>
        <button className={styles.registerBtn} onClick={() => navigate('/signin')}>
          {cta}
        </button>
      </div>

      {/* Lightbox */}
      {selected && (
        <div className={styles.overlay} onClick={() => setSelected(null)}>
          <div className={styles.lightbox} onClick={e => e.stopPropagation()}>
            <button className={styles.closeBtn} onClick={() => setSelected(null)}>✕</button>
            <img src={selected.img} alt={selected.label} className={styles.lightboxImg} />
            <div className={styles.lightboxInfo}>
              <p className={styles.lightboxLabel}>{selected.label}</p>
              <p className={styles.lightboxSub}>CLB Học Thuật • Hoạt động nổi bật</p>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}