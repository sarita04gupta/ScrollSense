package com.example.data.repository

import com.example.data.model.FeedSuggestion
import com.example.data.model.UserProfile

class FeedRecommendationEngine {

    private val masterFeedRepository = listOf(
        // Tech, Science & Innovation
        FeedSuggestion(
            id = "tech_1",
            title = "Veritasium & 3Blue1Brown",
            creatorOrHandle = "Derek Muller & Grant Sanderson",
            platform = "YouTube",
            category = "Science & Math",
            description = "Intuitive deep dives into physics, mathematics, quantum mysteries, and scientific history without sensationalism.",
            whyRecommended = "Stimulates genuine curiosity and neuroplasticity rather than high-frequency dopamine depletion.",
            targetGenders = listOf("All"),
            minAge = 14,
            maxAge = 70,
            actionUrl = "https://www.youtube.com/@veritasium",
            badge = "Deep Curiosity"
        ),
        FeedSuggestion(
            id = "tech_2",
            title = "Fireship & Lex Fridman Podcast",
            creatorOrHandle = "Jeff Delaney & Lex Fridman",
            platform = "YouTube",
            category = "Tech, AI & Society",
            description = "High-signal software architecture updates paired with long-form 3-hour dialogues with leading AI researchers.",
            whyRecommended = "Shifts your brain from 15-second fragmented attention to sustained contextual intellect.",
            targetGenders = listOf("Male", "Non-binary", "All"),
            minAge = 18,
            maxAge = 40,
            actionUrl = "https://www.youtube.com/@Fireship",
            badge = "High Signal"
        ),
        FeedSuggestion(
            id = "wellness_1",
            title = "Huberman Lab & Peter Attia Drive",
            creatorOrHandle = "Dr. Andrew Huberman & Dr. Peter Attia",
            platform = "Podcast",
            category = "Health & Longevity",
            description = "Protocols for sleep architecture, dopamine reset, cardiovascular VO2 max, and hormonal equilibrium.",
            whyRecommended = "Provides evidence-based tools specifically designed to overcome smartphone addiction and dopamine downregulation.",
            targetGenders = listOf("All"),
            minAge = 18,
            maxAge = 65,
            actionUrl = "https://www.youtube.com/@hubermanlab",
            badge = "Neuroscience"
        ),
        FeedSuggestion(
            id = "wellness_2",
            title = "Renaissance Periodization & Jeff Nippard",
            creatorOrHandle = "Dr. Mike Israetel & Jeff Nippard",
            platform = "YouTube",
            category = "Fitness & Hypertrophy",
            description = "Peer-reviewed resistance training, hypertrophy biomechanics, and nutrition science debunking fitness myths.",
            whyRecommended = "Channel scrolling impulses into physical self-mastery and strength progression.",
            targetGenders = listOf("Male", "All"),
            minAge = 16,
            maxAge = 50,
            actionUrl = "https://www.youtube.com/@RenaissancePeriodization",
            badge = "Evidence Based"
        ),
        FeedSuggestion(
            id = "wellness_3",
            title = "Move With Nicole & Natacha Océane",
            creatorOrHandle = "Nicole & Natacha Océane",
            platform = "YouTube",
            category = "Pilates, Mobility & Science",
            description = "Physiology-grounded functional mobility, posture realignment, calming Pilates, and metabolic health.",
            whyRecommended = "Reverses the sedentary forward-head posture caused by hours of downward phone staring.",
            targetGenders = listOf("Female", "All"),
            minAge = 15,
            maxAge = 55,
            actionUrl = "https://www.youtube.com/@MoveWithNicole",
            badge = "Posture & Flow"
        ),
        FeedSuggestion(
            id = "mindset_1",
            title = "The Daily Stoic & Ryan Holiday",
            creatorOrHandle = "Ryan Holiday",
            platform = "YouTube",
            category = "Stoicism & Mental Resilience",
            description = "Actionable ancient philosophy from Marcus Aurelius and Seneca on emotional control and guarding your time.",
            whyRecommended = "Teaches that your attention is your most irreplaceable asset—protect it from algorithmic capture.",
            targetGenders = listOf("All"),
            minAge = 16,
            maxAge = 75,
            actionUrl = "https://www.youtube.com/@DailyStoic",
            badge = "Mindful Mastery"
        ),
        FeedSuggestion(
            id = "career_1",
            title = "Ali Abdaal & Cal Newport",
            creatorOrHandle = "Deep Work & Productivity",
            platform = "YouTube",
            category = "Deep Work & Career",
            description = "Cultivating unbroken focus, building leverage in knowledge work, and eliminating digital distractions.",
            whyRecommended = "Learn tactical methods for achieving 4 hours of uninterrupted flow daily.",
            targetGenders = listOf("All"),
            minAge = 17,
            maxAge = 45,
            actionUrl = "https://www.youtube.com/@aliabdaal",
            badge = "Focus Fuel"
        ),
        FeedSuggestion(
            id = "creative_1",
            title = "Kurzgesagt – In a Nutshell",
            creatorOrHandle = "Kurzgesagt Team",
            platform = "YouTube",
            category = "Existential Science & Art",
            description = "Stunning hand-crafted animations exploring optimistic nihilism, space exploration, and human potential.",
            whyRecommended = "Replaces brainrot meme audio with sublime wonder, perspective, and world-class sound design.",
            targetGenders = listOf("All"),
            minAge = 13,
            maxAge = 80,
            actionUrl = "https://www.youtube.com/@kurzgesagt",
            badge = "Masterpiece"
        ),
        FeedSuggestion(
            id = "finance_1",
            title = "The Plain Bagel & Ben Felix",
            creatorOrHandle = "CFA Richard Coffin & Ben Felix",
            platform = "YouTube",
            category = "Sensible Wealth & Investing",
            description = "Rational, calm financial education free from crypto get-rich-quick scams or algorithmic hype.",
            whyRecommended = "Replaces flashy finance influencer reels with long-term compound wealth fundamentals.",
            targetGenders = listOf("All"),
            minAge = 18,
            maxAge = 60,
            actionUrl = "https://www.youtube.com/@ThePlainBagel",
            badge = "Anti-Hype Wealth"
        ),
        FeedSuggestion(
            id = "women_empower_1",
            title = "The Financial Diet & Simi Lindgren",
            creatorOrHandle = "Chelsea Fagan & Simi Lindgren",
            platform = "YouTube",
            category = "Financial Autonomy & Mindset",
            description = "Empowering career strategies, budgeting for life experiences, and dismantling lifestyle creep.",
            whyRecommended = "Practical financial confidence without influencer consumerism or cosmetic FOMO.",
            targetGenders = listOf("Female", "All"),
            minAge = 18,
            maxAge = 45,
            actionUrl = "https://www.youtube.com/@thefinancialdiet",
            badge = "Financial Zen"
        ),
        FeedSuggestion(
            id = "study_teens_1",
            title = "CrashCourse & Khan Academy",
            creatorOrHandle = "Hank Green & John Green",
            platform = "YouTube",
            category = "Academic Supercharger",
            description = "Engaging, curriculum-aligned lessons on world history, biology, chemistry, and literature.",
            whyRecommended = "Transform idle scrolling time into straight-A academic mastery.",
            targetGenders = listOf("All"),
            minAge = 13,
            maxAge = 22,
            actionUrl = "https://www.youtube.com/@crashcourse",
            badge = "Study Weapon"
        ),
        FeedSuggestion(
            id = "nature_detox_1",
            title = "BBC Earth & Wild Green Future",
            creatorOrHandle = "Sir David Attenborough",
            platform = "YouTube",
            category = "Nature & Calming Biosphere",
            description = "Spectacular slow-cinema wildlife documentaries with soothing natural soundscapes.",
            whyRecommended = "Lowers cortisol and autonomic sympathetic arousal after intense screen stimulation.",
            targetGenders = listOf("All"),
            minAge = 13,
            maxAge = 99,
            actionUrl = "https://www.youtube.com/@bbcearth",
            badge = "Cortisol Reset"
        )
    )

    fun getRecommendedFeeds(profile: UserProfile): List<FeedSuggestion> {
        val userAge = profile.age
        val userGender = profile.gender

        return masterFeedRepository.sortedByDescending { item ->
            var score = 0

            // Age match
            if (userAge in item.minAge..item.maxAge) {
                score += 5
            }

            // Gender relevance
            if (item.targetGenders.contains("All") || item.targetGenders.contains(userGender)) {
                score += 4
            }

            // Target specific demographic intersections
            if (userAge < 20 && (item.id == "study_teens_1" || item.id == "creative_1")) {
                score += 6
            }
            if (userGender.equals("Male", ignoreCase = true) && (item.id == "wellness_2" || item.id == "tech_2" || item.id == "mindset_1")) {
                score += 5
            }
            if (userGender.equals("Female", ignoreCase = true) && (item.id == "wellness_3" || item.id == "women_empower_1" || item.id == "creative_1")) {
                score += 5
            }
            if (userAge in 20..35 && (item.id == "career_1" || item.id == "finance_1" || item.id == "wellness_1")) {
                score += 5
            }

            score
        }
    }

    fun getDetoxPlanSummary(profile: UserProfile): String {
        val demographic = when {
            profile.age < 20 -> "young aspiring student (${profile.age}yo)"
            profile.age in 20..29 -> "ambitious ${profile.gender.lowercase()} (${profile.age}yo) entering peak career years"
            profile.age in 30..45 -> "high-performance ${profile.gender.lowercase()} (${profile.age}yo) balancing focus and life"
            else -> "seasoned lifelong learner (${profile.age}yo)"
        }

        return "As a $demographic, algorithmic short-form algorithms often target you with hyper-stimulating dopamine traps. Your personalized feed replaces empty scrolling with deliberate learning in longevity, career focus, and calm intellectual mastery."
    }
}
