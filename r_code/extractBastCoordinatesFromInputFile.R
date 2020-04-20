## Make sure your current packages are up to date
update.packages()


pacman::p_load(data.table, dplyr, ggplot2, reshape2)

#load_tn counts and compare with matsim assignment

##### read assignment results ############################################

workFolder = "./working/counts/input/"
file_counts = "bast_stations_2010.csv" ###Input file with observed counts from BASt stations
bast_stations = read.csv(paste(workFolder,file_counts, sep = ""), sep = ";")

bast_stations = bast_stations %>% select(Zst = DZ_Nr, Str_Kl, Fernziel_Ri1, Fernziel_Ri2, Koor_WGS84_N, Koor_WGS84_E, Koor_UTM32_E, Koor_UTM32_N)


write.csv(bast_stations, "./working/counts/input/bast_stations_with_coordinates.csv") ###Output file of the R code
