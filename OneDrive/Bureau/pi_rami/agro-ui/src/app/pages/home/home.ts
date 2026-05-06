import {
  Component,
  computed,
  signal,
  ChangeDetectionStrategy,
  inject
} from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { Router } from '@angular/router';
import { Navbar } from '../../shared/components/navbar/navbar';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [LucideAngularModule, Navbar],
  templateUrl: './home.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Home {
  private router = inject(Router);

  // =========================
  // STATE
  // =========================
  mobileMenuOpen = signal(false);
  activeFeature = signal<number>(0);

  // =========================
  // DATA
  // =========================
  features = [
    {
      id: 0,
      title: "Analyse du Sol",
      description: "Évaluez les propriétés agronomiques de votre terrain avec précision scientifique.",
      gradient: "from-green-600 to-emerald-500",
      icon: "Leaf", 
      image: "🌱"
    },
    {
      id: 1,
      title: "Risque Climatique",
      description: "Prédisez l'impact des conditions météorologiques sur vos cultures.",
      gradient: "from-blue-600 to-cyan-500",
      icon: "CloudRain",
      image: "🌧️"
    },
    {
      id: 2,
      title: "NDVI Satellite",
      description: "Analysez la productivité de votre terrain grâce aux données satellite.",
      gradient: "from-purple-600 to-pink-500",
      icon: "Satellite",
      image: "🛰️"
    },
    {
      id: 3,
      title: "Analyse de Marché",
      description: "Comprenez les tendances du marché en temps réel.",
      gradient: "from-orange-600 to-red-500",
      icon: "TrendingUp",
      image: "📊"
    },
    {
      id: 4,
      title: "Recommandation IA",
      description: "Obtenez des recommandations intelligentes basées sur vos données.",
      gradient: "from-green-600 to-teal-500",
      icon: "Cpu",
      image: "🤖"
    }
  ];

  // =========================
  // METHODS
  // =========================

  setActiveFeature(id: number): void {
    this.activeFeature.set(id);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(v => !v);
  }

  scrollToSection(id: string): void {
    const element = document.getElementById(id);

    if (element) {
      const offset = -80; // navbar height
      const y =
        element.getBoundingClientRect().top +
        window.scrollY +
        offset;

      window.scrollTo({
        top: y,
        behavior: 'smooth'
      });
    }

    this.mobileMenuOpen.set(false);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // =========================
  // COMPUTED
  // =========================
  currentFeature = computed(() => {
    return this.features?.[this.activeFeature()] ?? this.features[0];
  });
}