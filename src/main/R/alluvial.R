#install.packages("ggalluvial")
require(ggalluvial)
require(RColorBrewer)

setwd("/Users/joeri/")
vch <- read.table("vkgl_consensus_history_df.tsv",header=TRUE,sep='\t',quote="",comment.char="")
#is_lodes_form(vch, key = "Release", value = "Consensus", id = "Id")

vch$Release <- factor(vch$Release,levels = c("May 2018", "October 2018", "June 2019", "October 2019", "December 2019", "March 2020", "June 2020", "September 2020", "January 2021"))

extendPalette <- c(brewer.pal(8, "Set2"), "#000000")

ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = extendPalette) +
  geom_flow() + #stat = "alluvium", lode.guidance = "frontback" to trace variants
  geom_stratum() +
  theme_bw() +
  theme(panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
  ggtitle("VKGL national diagnostics variant database: what happens to variants with an opposite classification in one or more releases?")
