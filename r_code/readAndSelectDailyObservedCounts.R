pacman::p_load(data.table, dplyr, ggplot2)

#read station data

input_folder = "./working/counts/input/"

file_motorway = "bast_stations_2010.csv" ###Input file with the observed BASt counts

motorway_counts = fread(paste(input_folder, file_motorway, sep = ""))
names(motorway_counts)

motorway_counts = motorway_counts %>%
  select(Zst = DZ_Nr, Land = Land_Nr, LKW_R1 = DTV_SV_MobisSo_Ri1, LKW_R2 = DTV_SV_MobisSo_Ri2)