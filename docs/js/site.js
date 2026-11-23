(() => {
  function initMenu() {
    const toggle = document.querySelector("[data-menu-toggle]");
    const menu = document.querySelector("[data-site-menu]");
    if (!toggle || !menu) return;

    toggle.hidden = false;
    menu.classList.add("is-collapsible");

    const closeMenu = () => {
      menu.classList.remove("is-open");
      toggle.setAttribute("aria-expanded", "false");
      toggle.setAttribute("aria-label", "Open navigation");
    };

    toggle.addEventListener("click", () => {
      const nextState = toggle.getAttribute("aria-expanded") !== "true";
      menu.classList.toggle("is-open", nextState);
      toggle.setAttribute("aria-expanded", String(nextState));
      toggle.setAttribute(
        "aria-label",
        nextState ? "Close navigation" : "Open navigation",
      );
    });

    menu.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", closeMenu);
    });

    document.addEventListener("keydown", (event) => {
      if (event.key !== "Escape" || !menu.classList.contains("is-open")) return;
      closeMenu();
      toggle.focus();
    });

    document.addEventListener("click", (event) => {
      if (!menu.classList.contains("is-open")) return;
      if (menu.contains(event.target) || toggle.contains(event.target)) return;
      closeMenu();
    });
  }

  function initScreenshotSlider() {
    const slider = document.querySelector("[data-screenshot-slider]");
    if (!slider) return;

    const track = slider.querySelector("[data-slider-track]");
    const slides = Array.from(slider.querySelectorAll(".screenshot-slide"));
    const prev = slider.querySelector("[data-slider-prev]");
    const next = slider.querySelector("[data-slider-next]");
    const progress = slider.querySelector("[data-slider-progress]");
    const duration = 10000;
    let index = 0;
    let timer = 0;
    let paused = false;

    if (!track || slides.length < 2 || !progress) return;

    const isPointerInside = () =>
      slider.matches(":hover") || slider.contains(document.activeElement);

    const resetProgress = () => {
      progress.style.transition = "none";
      progress.style.width = "0";
    };

    const startProgress = () => {
      resetProgress();
      window.requestAnimationFrame(() => {
        if (paused) return;
        progress.style.transition = `width ${duration}ms linear`;
        progress.style.width = "100%";
      });
    };

    const schedule = () => {
      window.clearTimeout(timer);
      if (paused || isPointerInside()) {
        resetProgress();
        return;
      }
      startProgress();
      timer = window.setTimeout(() => {
        if (isPointerInside()) {
          paused = true;
          resetProgress();
          return;
        }
        goTo(index + 1);
      }, duration);
    };

    function goTo(nextIndex) {
      index = (nextIndex + slides.length) % slides.length;
      track.style.transform = `translateX(${-index * 100}%)`;
      schedule();
    }

    const pause = () => {
      paused = true;
      window.clearTimeout(timer);
      resetProgress();
    };

    const resume = () => {
      if (!paused) return;
      paused = false;
      schedule();
    };

    prev?.addEventListener("click", () => goTo(index - 1));
    next?.addEventListener("click", () => goTo(index + 1));
    slider.addEventListener("pointerenter", pause);
    slider.addEventListener("pointermove", pause);
    slider.addEventListener("pointerleave", resume);
    slider.addEventListener("mouseenter", pause);
    slider.addEventListener("mousemove", pause);
    slider.addEventListener("mouseleave", resume);
    slider.addEventListener("focusin", pause);
    slider.addEventListener("focusout", (event) => {
      if (slider.contains(event.relatedTarget)) return;
      resume();
    });

    schedule();
  }

  function initFaqPage() {
    if (document.body.dataset.page !== "faq") return;

    const toolbar = document.querySelector("[data-faq-toolbar]");
    const list = document.querySelector("[data-faq-list]");
    const status = document.querySelector("[data-faq-status]");
    const search = document.querySelector("[data-faq-search]");
    const tabs = Array.from(
      document.querySelectorAll("[data-faq-tabs] button"),
    );
    if (!toolbar || !list || !status || !search || !tabs.length) return;

    const items = Array.from(list.querySelectorAll(".faq-item")).map(
      (item) => ({
        element: item,
        category: item.dataset.category,
        searchText: `${item.textContent} ${item.dataset.tags}`
          .replace(/\s+/g, " ")
          .toLowerCase(),
      }),
    );
    let activeCategory = "all";

    function filterItems() {
      const query = search.value.trim().toLowerCase();
      let count = 0;

      items.forEach(({ element, category, searchText }) => {
        const isVisible =
          (activeCategory === "all" || category === activeCategory) &&
          searchText.includes(query);
        element.hidden = !isVisible;
        element.open = isVisible && count === 0;
        if (isVisible) count += 1;
      });

      status.textContent = count
        ? `${count} question${count === 1 ? "" : "s"}`
        : "No matching questions.";
      status.classList.toggle("is-empty", count === 0);
    }

    tabs.forEach((tab) => {
      tab.addEventListener("click", () => {
        activeCategory = tab.dataset.category;
        tabs.forEach((button) => {
          button.setAttribute("aria-pressed", String(button === tab));
        });
        filterItems();
      });
    });

    search.addEventListener("input", filterItems);
    toolbar.hidden = false;
  }

  function formatSupporterDate(value) {
    if (typeof value !== "string") return "";
    const parts = value.split("-");
    if (parts.length !== 3) return value;
    const [year, month, day] = parts;
    if (year.length !== 4 || month.length !== 2 || day.length !== 2)
      return value;
    return `${day}.${month}.${year}`;
  }

  function createSupporterCard(supporter) {
    const article = document.createElement("article");
    article.className = "supporter-card";

    const header = document.createElement("div");
    header.className = "supporter-card-head";

    const avatar = document.createElement("span");
    avatar.className = "supporter-avatar";
    avatar.textContent =
      String(supporter.name || "?")
        .trim()
        .charAt(0)
        .toUpperCase() || "?";

    const name = document.createElement("strong");
    name.className = "supporter-name";
    name.textContent = supporter.name || "Someone";

    const date = document.createElement("span");
    date.className = "supporter-date";
    date.textContent = formatSupporterDate(supporter.date);

    header.append(avatar, name, date);
    article.append(header);

    if (supporter.message) {
      const message = document.createElement("p");
      message.className = "supporter-message";
      message.textContent = supporter.message;
      article.append(message);
    }

    return article;
  }

  async function initDonatePage() {
    if (document.body.dataset.page !== "donate") return;

    const list = document.querySelector("[data-supporters-list]");
    const status = document.querySelector("[data-supporters-status]");
    const count = document.querySelector("[data-supporters-count]");
    const latest = document.querySelector("[data-supporters-latest]");
    if (!list) return;

    try {
      const response = await fetch("supporters.json", { cache: "no-store" });
      if (!response.ok)
        throw new Error(`Supporters request failed: ${response.status}`);

      const rawSupporters = await response.json();
      const supporters = rawSupporters
        .filter((item) => item && item.name && item.date)
        .sort((left, right) => {
          const byDate =
            new Date(right.date).getTime() - new Date(left.date).getTime();
          return byDate || Number(right.id || 0) - Number(left.id || 0);
        });

      if (count) count.textContent = String(supporters.length);
      if (latest)
        latest.textContent = supporters[0]
          ? formatSupporterDate(supporters[0].date)
          : "None yet";

      list.replaceChildren();
      supporters.forEach((supporter) => {
        list.append(createSupporterCard(supporter));
      });

      if (status) {
        status.textContent = supporters.length
          ? ""
          : "No supporters are listed yet.";
        status.classList.toggle("is-hidden", supporters.length > 0);
      }
    } catch (error) {
      if (status) {
        status.textContent = "Supporter list is temporarily unavailable.";
        status.classList.remove("is-hidden");
      }
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    initMenu();
    initScreenshotSlider();
    initFaqPage();
    initDonatePage();
  });
})();
