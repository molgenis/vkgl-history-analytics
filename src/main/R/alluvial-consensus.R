#install.packages("ggalluvial")
#install.packages("plyr")
require(ggplot2)
require(ggalluvial)
require(RColorBrewer)
require(plyr)

setwd("/Users/joeri/VKGL/VKGL-releases/july2025")
curRel <- "july2025"
curRelFull <- "July 2025"

vch <- read.table("dataframe.tsv",header=TRUE,sep='\t',quote="",comment.char="")
vch$Release <- factor(vch$Release, levels = c("may2018", "oct2018", "june2019", "oct2019", "dec2019", "mar2020", "jun2020", "sep2020", "apr2021", "jun2021", "sep2021", "dec2021", "sep2022", "jan2023", "apr2023", "july2023", "oct2023", "feb2024", "apr2024", "july2024", "oct2024", "jan2025", "apr2025", "july2025"))
vch$Release <- revalue(vch$Release, c("may2018"="May 2018", "oct2018"="Oct 2018", "june2019"="June 2019", "oct2019"="Oct 2019", "dec2019"="Dec 2019", "mar2020"="Mar 2020", "jun2020"="June 2020", "sep2020"="Sept 2020", "apr2021"="Apr 2021", "jun2021"="June 2021", "sep2021"="Sept 2021", "dec2021"="Dec 2021", "sep2022"="Sept 2022", "jan2023"="Jan 2023", "apr2023"="Apr 2023", "july2023"="July 2023", "oct2023"="Oct 2023", "feb2024"="Feb 2024", "apr2024"="Apr 2024", "july2024"="July 2024", "oct2024"="Oct 2024", "jan2025"="Jan 2025", "apr2025"="Apr 2025", "july2025"="July 2025"))
vch$Consensus <- factor(vch$Consensus)
vch$Consensus <- revalue(vch$Consensus, c("VUS"="VUS", "LB"="LB/B", "LP"="LP/P", "CF"="Multiple classifications", "Absent"="Absent from release"))

palette <- c( "VUS" = "#8DA0CB",
              "LB/B" =  "#A6D854",
              "LP/P" =  "#FC8D62",
              "Multiple classifications" =  "#FFD92F",
              "Absent from release" = "#B3B3B3")

ggplot(vch, aes(x = Release, stratum = Consensus, alluvium = Id, fill = Consensus, label = Consensus)) +
  scale_fill_manual(values = palette) +
  geom_flow() +
  geom_stratum(colour=NA) +
  theme_bw() +
  theme(legend.title = element_blank(), panel.grid = element_blank(), panel.border = element_rect(colour = "black"), axis.ticks = element_line(colour = "black"), axis.text = element_text(color = "black")) +
  theme(legend.position = "bottom") +
  scale_x_discrete(guide = guide_axis(n.dodge = 2)) +
  labs(x = "Release date of variant classification database", y = "Number of variants") +
#ggtitle(paste("Classification history of all variants in the VKGL ",curRelFull," public consensus release", sep=""))
#ggsave(paste("vkgl-",curRel,".png", sep=""), width = 11, height = 6)

#ggtitle(paste("History of variants in the VKGL ",curRelFull," public consensus release with >1 different lifetime classifications", sep=""))
#ggsave(paste("vkgl-",curRel,"-gt1clsf.png", sep=""), width = 11, height = 6)

#ggtitle(paste("History of variants that have appeared in the VKGL public consensus that are not part of the ",curRelFull," release", sep=""))
#ggsave(paste("vkgl-notin",curRel,".png",sep=""), width = 11, height = 6)

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
  scale_x_discrete(guide = guide_axis(n.dodge = 2)) +
  labs(x = "Release date of VKGL variant classification database export (public consensus)", y = "Number of variants") +
#ggtitle(paste("History of variants in the VKGL ",curRelFull," public consensus release with any lifetime LP-to-LB or LB-to-LP transition",sep=""))
#ggsave(paste("vkgl-",curRel,"-lp-lb-trans.png",sep=""), width = 11, height = 7)

#ggtitle("History of Y-chromosome variants that have appeared in any VKGL public consensus release")
#ggsave(paste("vkgl-",curRel,"-y.png",sep=""), width = 11, height = 6)

#ggtitle(paste("History of BRCA1 insertion variants in the VKGL ",curRelFull," public consensus release", sep=""))
#ggsave(paste("vkgl-",curRel,"-brca1-ins.png",sep=""), width = 11, height = 6)

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
  scale_x_discrete(guide = guide_axis(n.dodge = 2)) +
  labs(x = "Release date of VKGL variant classification database export (public consensus)", y = "Number of variants") +
#ggtitle(paste("History of variants in the VKGL ",curRelFull," public consensus release with multiple classifications", sep=""))
#ggsave(paste("vkgl-",curRel,"-multiclass.png", sep=""), width = 11, height = 8)

#ggtitle(paste("History of SAID gene panel indel variants in the VKGL ",curRelFull," public consensus release", sep=""))
#ggsave(paste("vkgl-",curRel,"-said-indels.png", sep=""), width = 11, height = 8)


#################
# PAPER NUMBERS #
#################

# on latest 'unfiltered' data:
total <- dim(vch)[1]
total
table(vch$Consensus)
SLCO1B1_occ <- dim(subset(vch, Gene == "SLCO1B1"))[1]
# on 'greater than 1 classification'
dim(subset(vch, Release == "July 2024"))
oct18lblp <- subset(vch, Release == "Oct 2018" & (Consensus == "LB/B" | Consensus == "LP/P"))
dim(oct18lblp)
jun19vus <- subset(vch, Release == "June 2019" & Consensus == "VUS")
dim(jun19vus)
oct18lblpToJun19vus <- intersect(oct18lblp$Id, jun19vus$Id)
length(oct18lblpToJun19vus)
oct19lblp <- subset(vch, Release == "Oct 2019" & (Consensus == "LB/B" | Consensus == "LP/P"))
jun19vusToOct19lblp <- intersect(jun19vus$Id,oct19lblp$Id)
length(jun19vusToOct19lblp)
length(intersect(oct18lblpToJun19vus,jun19vusToOct19lblp))
# on lp-lb or lb-lp transitions
latest_data <- subset(vch, Release == "July 2024")
latest_total <- dim(latest_data)[1]
latest_total
latest_SLCO1B1_occ <- dim(subset(latest_data, Gene == "SLCO1B1"))[1]
latest_SLCO1B1_occ
dat <- data.frame("SLCO1B1" = c(SLCO1B1_occ, latest_SLCO1B1_occ), "OtherGenes" = c(total-SLCO1B1_occ, latest_total-latest_SLCO1B1_occ), row.names = c("Latest full data", "Latest data LB/LP transitions"), stringsAsFactors = FALSE)
dat
t <- fisher.test(dat)
t$p.value


##########
# UNUSED #
##########

# Proportion of variants in a specific release
rel <- subset(vch, Release == "May 2018")
rel <- subset(vch, Release == "Oct 2023")
table(rel$Consensus)

# Quantify and print number of changes between releases
for(start in 1:(length(levels(vch$Release))-1))
{
  first <- levels(vch$Release)[start]
  second <- levels(vch$Release)[start+1]
  firstIDs <- vch[vch$Release == first,]$Consensus
  secondIDs <- vch[vch$Release == second,]$Consensus
  changes <- length(firstIDs) - sum(firstIDs == secondIDs)
  cat(paste("from",first,"to",second,"there are",changes,"changes\n",sep=" "))
}

# Variants NOT absent in release A but absent in release B
a <- subset(vch, Release == "Oct 2019" & Consensus != "Absent from release")
table(a$Consensus)
b <- subset(vch, Release == "Dec 2019" & Consensus == "Absent from release")
table(b$Consensus)
sum(a$Id %in% b$Id)

# Fisher test of overrepresented SLCO1B1 mutations
dat <- data.frame("SLCO1B1" = c(244, 17), "OtherGenes" = c(203882-244, 99-17), row.names = c("Oct 2023 full data", "Oct 2023 LB/LP transitions"), stringsAsFactors = FALSE)
t <- fisher.test(dat)
t$p.value
