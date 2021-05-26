#install.packages("ggalluvial")
require(ggalluvial)
require(RColorBrewer)

setwd("/Users/joeri/github/vkgl-history-analytics/artifacts/")
vch <- read.table("vkgl_consensus_history_df_v2.tsv",header=TRUE,sep='\t',quote="",comment.char="")
#is_lodes_form(vch, key = "Release", value = "Consensus", id = "Id")

vch$Release <- factor(vch$Release,levels = c("May 2018", "October 2018", "June 2019", "October 2019", "December 2019", "March 2020", "June 2020", "September 2020", "January 2021"))
#vch$Consensus <- factor(vch$Consensus,levels = c("One lab: (Likely) benign", "One lab: VUS", "One lab: (Likely) pathogenic", "Multiple labs: (Likely) benign", "Multiple labs: VUS", "Multiple labs: (Likely) pathogenic", "Absent from release", "Multiple labs: No consensus", "Multiple labs: Opposite classifications"))
vch$Consensus <- factor(vch$Consensus,levels = c("Multiple labs: (Likely) benign", "One lab: (Likely) benign", "Multiple labs: VUS", "One lab: VUS", "Multiple labs: (Likely) pathogenic", "One lab: (Likely) pathogenic", "Absent from release", "Multiple labs: No consensus", "Multiple labs: Opposite classifications"))

#extendPalette <- c(brewer.pal(8, "Set2"), "#000000")

extendPalette <- c( "Multiple labs: Opposite classifications" = "#FFD92F",
                    "Multiple labs: VUS" = "#E5C494",
                    "One lab: (Likely) benign" = "#66C2A5",
                    "One lab: (Likely) pathogenic" = "#E78AC3",
                    "Multiple labs: (Likely) benign" = "#A6D854",
                    "One lab: VUS" = "#FC8D62",
                    "Multiple labs: (Likely) pathogenic" = "brown",
                    "Multiple labs: No consensus" = "#8DA0CB",
                    "Absent from release" = "#B3B3B3")
                    
## default: aggregated

ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = extendPalette) +
  geom_flow() +
  geom_stratum() +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
  ggtitle("VKGL national diagnostics variant database: what happened to variants with opposite classifications in one or more releases?")
ggsave("vkgl_opposite_history_v2.png", width = 11, height = 6)

## special: variants can be followed
ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = extendPalette) +
  geom_flow(stat = "alluvium", lode.guidance = "frontback", size=0.1, color="black") +
  geom_stratum() +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
  ggtitle("VKGL national diagnostics variant database: what happened to variants with opposite classifications in one or more releases?")
ggsave("vkgl_opposite_history_pervar_v2.png", width = 11, height = 6)
