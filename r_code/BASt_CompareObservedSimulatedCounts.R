## Make sure your current packages are up to date
update.packages()

pacman::p_load(data.table, dplyr, ggplot2, reshape2)

#load_tn counts and compare with matsim assignment

##### read assignment results ############################################

workFolder = "./output/ld_all_2011/"
file_counts = "counts_multi_day2_withBastStations.csv" ###Output file with the hourly counts, produced by Matsim
matsim_volumes = read.csv(paste(workFolder,file_counts, sep = ""))

matsim_volumes = matsim_volumes %>% select(LINK = link, hour, lDTruck)
matsim_volumes$count_matsim = matsim_volumes$lDTruck * 20 * 0.68

matsim_volumes1 = matsim_volumes %>%
  select(linkDirection1 = LINK,hour, count_matsim)
matsim_volumes2 = matsim_volumes %>%
  select(linkDirection2 = LINK,hour, count_matsim)

matsim_volumes1$hour = matsim_volumes1$hour + 1
matsim_volumes2$hour = matsim_volumes2$hour + 1

#add the >24 hour counts to today counts 

matsim_volumes1 = matsim_volumes1 %>% 
  mutate(hour = if_else(hour > 24, hour - 24, hour)) %>%
  mutate(hour = if_else(hour > 24, hour - 24, hour)) %>%
  group_by(linkDirection1, hour) %>% summarize(count_matsim1 = sum(count_matsim))

matsim_volumes2 = matsim_volumes2 %>% 
  mutate(hour = if_else(hour > 24, hour - 24, hour)) %>%
  mutate(hour = if_else(hour > 24, hour - 24, hour)) %>%
  group_by(linkDirection2, hour) %>% summarize(count_matsim2 = sum(count_matsim))



##### read link - station relations ############################################

#links of the stations
links_stations_folder = "./networks/matsim/"
links_stations_file = "stationsWithMatsimLinksCORRECTED.csv" ###Output file with the stations and corresponding links
links_stations = fread(paste(links_stations_folder, links_stations_file, sep  = ""))
links_stations = links_stations %>%
  select(Zst = ID, longitude, latitude, direction1, linkDirection1, direction2, linkDirection2)

##### merge link - station and matsim###########################################

links_stations1 = merge(x = links_stations, y = matsim_volumes1, by  = "linkDirection1")
links_stations1 = merge(x = links_stations1, y = matsim_volumes2, by  = c("linkDirection2","hour"))


##### merge links at the same station ##########################################

#creating a dataset with the difference and counts
stations_comparison = links_stations1 %>%
  group_by(Zst, direction1, direction2, longitude, latitude) %>%
  summarize(sim_link_count = n() ,
            sim_link_sum1 = sum(count_matsim1), sim_link_sum2 = sum(count_matsim2))

##### read observed 2010 counts ################################################

source("./r_code/readAndSelectDailyObservedCounts.R") ###Code selecting the relevant observed data from the BASt file

stations_comparison = merge(stations_comparison, motorway_counts, by = c("Zst"))

stations_comparison$count_difference1 = stations_comparison$sim_link_sum1 - stations_comparison$LKW_R1
stations_comparison$count_difference2 = stations_comparison$sim_link_sum2 - stations_comparison$LKW_R2

write.csv(stations_comparison, "./r_code/bast_counts_comparison.csv") ###Output file of this R code