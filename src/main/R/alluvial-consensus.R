#install.packages("ggalluvial")
install.packages("plyr")
require(ggalluvial)
require(RColorBrewer)
require(plyr)

setwd("/Users/joeri/VKGL-releases")
vch <- read.table("dataframe.tsv",header=TRUE,sep='\t',quote="",comment.char="")
vch$Release <- factor(vch$Release, levels = c("may2018", "oct2018", "june2019", "oct2019", "dec2019", "mar2020", "jun2020", "sep2020", "apr2021", "jun2021", "sep2021", "dec2021", "sep2022"))
vch$Release <- revalue(vch$Release, c("may2018"="May 2018", "oct2018"="Oct 2018", "june2019"="June 2019", "oct2019"="Oct 2019", "dec2019"="Dec 2019", "mar2020"="Mar 2020", "jun2020"="June 2020", "sep2020"="Sept 2020", "apr2021"="Apr 2021", "jun2021"="June 2021", "sep2021"="Sept 2021", "dec2021"="Dec 2021", "sep2022"="Sept 2022"))
vch$Consensus <- factor(vch$Consensus)

ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  geom_flow() +
  geom_stratum() +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
  ggtitle("todo")

ggsave("vkgl_consensus_history.png", width = 11, height = 6)

## special: variants can be followed
ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  geom_flow(stat = "alluvium", lode.guidance = "frontback", size=0.1, color="black") +
  geom_stratum() +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
  ggtitle("todo")

ggsave("vkgl_opposite_history_pervar_v2.png", width = 11, height = 6)
