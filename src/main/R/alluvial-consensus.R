#install.packages("ggalluvial")
#install.packages("plyr")
require(ggalluvial)
require(RColorBrewer)
require(plyr)

setwd("/Users/joeri/VKGL-releases")
vch <- read.table("dataframe.tsv",header=TRUE,sep='\t',quote="",comment.char="")
vch$Release <- factor(vch$Release, levels = c("may2018", "oct2018", "june2019", "oct2019", "dec2019", "mar2020", "jun2020", "sep2020", "apr2021", "jun2021", "sep2021", "dec2021", "sep2022", "jan2023", "apr2023"))
vch$Release <- revalue(vch$Release, c("may2018"="May 2018", "oct2018"="Oct 2018", "june2019"="June 2019", "oct2019"="Oct 2019", "dec2019"="Dec 2019", "mar2020"="Mar 2020", "jun2020"="June 2020", "sep2020"="Sept 2020", "apr2021"="Apr 2021", "jun2021"="June 2021", "sep2021"="Sept 2021", "dec2021"="Dec 2021", "sep2022"="Sept 2022", "jan2023"="Jan 2023", "apr2023"="Apr 2023"))
vch$Consensus <- factor(vch$Consensus)
vch$Consensus <- revalue(vch$Consensus, c("VUS"="VUS", "LB"="LB/B", "LP"="LP/P", "CF"="No consensus", "Absent"="Absent from release"))

palette <- c( "VUS" = "#8DA0CB",
              "LB/B" =  "#A6D854",
              "LP/P" =  "#FC8D62",
              "No consensus" =  "#FFD92F",
              "Absent from release" = "#B3B3B3")

ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = palette) +
  geom_flow() +
  geom_stratum() +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
#ggtitle("Classification history of all variants in the VKGL April 2023 public consensus release")
#ggsave("vkgl-apr2023.png", width = 11, height = 6)

#ggtitle("History of variants in the VKGL April 2023 public consensus release with >1 different lifetime classifications")
#ggsave("vkgl-apr2023-gt1clsf.png", width = 11, height = 6)

ggtitle("History of variants that have appeared in the VKGL public consensus that are not part of the April 2023 release")
ggsave("vkgl-notinapr2023.png", width = 11, height = 6)

---  

## special: variants can be followed
#geom_text(stat = "alluvium", discern = FALSE, size = 2, aes(label = after_stat(alluvium))) +
ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = palette) +
  geom_stratum(colour=NA) +
  geom_flow(stat = "alluvium", linewidth=0.1) +
  geom_text(stat = "alluvium", aes(label = Label), size = 1.2) +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of VKGL variant classification database export (public consensus)", y = "Number of variants") +
#ggtitle("History of variants in the VKGL April 2023 public consensus release with any lifetime LP-to-LB or LB-to-LP transition")
#ggsave("vkgl-apr2023-lp-lb-trans.png", width = 11, height = 6)

ggtitle("History of Y-chromosome variants that have appeared in any VKGL public consensus release")
ggsave("vkgl-apr2023-y.png", width = 11, height = 6)

---

## same as above but different size/scaling to allow for more data
ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = palette) +
  geom_stratum(colour=NA) +
  geom_flow(stat = "alluvium", linewidth=0.1) +
  geom_text(stat = "alluvium", aes(label = Label), size = 0.8) +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of VKGL variant classification database export (public consensus)", y = "Number of variants") +
ggtitle("History of variants in the VKGL April 2023 public consensus release without a consensus classification")
ggsave("vkgl-apr2023-nocons.png", width = 11, height = 8)
