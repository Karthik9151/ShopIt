import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import App from './App';

describe('global layout accessibility', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/');
  });

  afterEach(() => cleanup());

  it('provides a skip link, labeled navigation landmark, and main target', () => {
    render(<App />);

    expect(screen.getByRole('link', { name: 'Skip to main content' })).toBeTruthy();
    expect(screen.getByRole('navigation', { name: 'Primary navigation' })).toBeTruthy();

    const main = screen.getByRole('main');
    expect(main.getAttribute('id')).toBe('main-content');
    expect(main.getAttribute('tabindex')).toBe('-1');
  });

  it('announces whether the mobile navigation is expanded', () => {
    render(<App />);

    const toggle = screen.getByRole('button', { name: 'Toggle navigation' });
    expect(toggle.getAttribute('aria-expanded')).toBe('false');

    fireEvent.click(toggle);
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
  });
});
