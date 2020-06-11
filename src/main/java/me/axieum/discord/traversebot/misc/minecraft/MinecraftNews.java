package me.axieum.discord.traversebot.misc.minecraft;

import me.axieum.discord.traversebot.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MinecraftNews extends TimerTask
{
    private static final Timer TIMER = new Timer("MinecraftNews");
    private static final String MINECRAFT_RSS_URL = "https://minecraft.net/en-us/feeds/community-content/rss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E, d MMM y H:m:s Z");

    private final JDA discord;
    private ZonedDateTime since = ZonedDateTime.now().minusDays(1);

    /**
     * Initialises and begins the Minecraft News task.
     */
    public static void init(JDA discord)
    {
        // Fetch frequency
        final Duration freq = Duration.ofMinutes(Config.getConfig().getInt("minecraft.news.frequency"));
        // Dispatch scheduled task
        TIMER.scheduleAtFixedRate(new MinecraftNews(discord), 0, freq.toMillis());
    }

    /**
     * Constructs a new Minecraft News timer task.
     *
     * @param jda Discord JDA client
     */
    public MinecraftNews(JDA jda)
    {
        this.discord = jda;
    }

    /**
     * Runs the Minecraft News task, fetching recent
     * articles and pushing them to Discord.
     *
     * @inheritDoc
     */
    @Override
    public void run()
    {
        // Fetch server channel (if specified) to publish news items to
        long channelId = Config.getConfig().get("minecraft.news.channel");
        if (channelId < 0) {
            System.out.println("Skip fetching Minecraft news articles");
            return;
        }

        // Fetch Discord channel
        TextChannel channel = discord.getTextChannelById(channelId);
        if (channel == null) {
            System.err.printf("Cannot push Minecraft news articles to the channel with id %s\n", channelId);
            return;
        }

        // Fetch article tags to observe
        String[] tags = Config.getConfig().get("minecraft.news.tags");
        final Predicate<String> tagsPredicate = tags == null || tags.length < 1 ?
                                                Pattern.compile(".*").asPredicate() :
                                                Pattern.compile(String.join("|", tags)).asPredicate();

        try {
            // Load the RSS feed
            System.out.println("Checking for new Minecraft news articles...");
            Jsoup.connect(MINECRAFT_RSS_URL)
                 .get()
                 // Pluck article items
                 .select("item")
                 .stream()
                 // Filter relevant tags
                 .filter(element -> {
                     final Element tagEle = element.selectFirst("primarytag");
                     return tagEle != null && tagsPredicate.test(tagEle.ownText());
                 })
                 // Filter recent entries
                 .filter(element -> {
                     try {
                         final Element pubDateEl = element.selectFirst("pubdate");
                         return pubDateEl != null &&
                                ZonedDateTime.parse(pubDateEl.ownText(), DATE_FORMATTER).isAfter(since);
                     } catch (DateTimeParseException e) {
                         return false;
                     }
                 })
                 // Push filtered articles
                 .forEach(element -> pushArticle(channel, element));
        } catch (IOException e) {
            System.err.printf("Unable to fetch the Minecraft news feed: %s\n", e.getMessage());
            e.printStackTrace();
        }

        // Wrap-up, remember the time we last fetched articles
        System.out.printf("Scheduled to check for more Minecraft news articles in %d minutes\n",
                          Config.getConfig().getInt("minecraft.news.frequency"));
        this.since = ZonedDateTime.now();
    }

    /**
     * Publishes a given RSS article item to Discord.
     *
     * @param channel Discord channel to push article to
     * @param element XML element article item
     */
    private void pushArticle(TextChannel channel, Element element)
    {
        // Extract relevant metadata
//        final String title = element.selectFirst("title").ownText();
        final String link = element.selectFirst("link").ownText();
//        final String description = element.selectFirst("description").ownText();
        final String imageUrl = StringUtil.resolve(element.baseUri(), element.selectFirst("imageurl").ownText());
        final ZonedDateTime publishDate = ZonedDateTime.parse(element.selectFirst("pubdate").ownText(), DATE_FORMATTER);

        // Web-scrape further metadata
        final String title, extract, author, avatarUrl;
        try {
            System.out.printf("Scraping article content from %s\n", link);
            final Document document = Jsoup.connect(link).get();

            // Title and body
            title = document.selectFirst("h1").ownText();
            extract = WordUtils.abbreviate(document.selectFirst(".text").text(),
                                           Config.getConfig().getInt("minecraft.news.extract_length"),
                                           MessageEmbed.TEXT_MAX_LENGTH,
                                           "...");

            // Author
            final Element attribution = document.selectFirst(".article-attribution-container");
            if (attribution != null) {
                author = attribution.selectFirst(".attribution__details dd").ownText();
                final Element avatarEle = attribution.selectFirst(".attribution__avatar img");
                avatarUrl = avatarEle != null ? avatarEle.attr("abs:src") : null;
            } else {
                author = avatarUrl = null;
            }
        } catch (IOException e) {
            System.err.printf("Unable to scrape article contents: %s\n", e.getMessage());
            e.printStackTrace();
            return;
        }

        // Fetch Embed configurations
        final int embedColor = Config.getConfig().get("minecraft.news.color");
        final String embedAuthorName = Config.getConfig().get("minecraft.news.author.name");
        final String embedAuthorUrl = Config.getConfig().get("minecraft.news.author.url");
        final String embedAuthorIcon = Config.getConfig().get("minecraft.news.author.icon");

        // Construct Discord embedding
        EmbedBuilder embed = new EmbedBuilder().setTitle(title, link)
                                               .setImage(imageUrl)
                                               .setDescription(extract)
                                               .setFooter(author, avatarUrl)
                                               .setColor(embedColor)
                                               .setAuthor(embedAuthorName.isEmpty() ? null : embedAuthorName,
                                                          embedAuthorUrl.isEmpty() ? null : embedAuthorUrl,
                                                          embedAuthorIcon.isEmpty() ? null : embedAuthorIcon)
                                               .setTimestamp(publishDate);

        // Push the article embedding!
        System.out.printf("Pushing article to Discord: %s\n", title);
        channel.sendMessage(embed.build()).queue();
    }
}
