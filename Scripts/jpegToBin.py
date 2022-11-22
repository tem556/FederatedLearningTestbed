#!/usr/bin/python3

import glob
import itertools
import cv2
import matplotlib.pyplot as plt


Height = 180
Width  = 180
NumberOfPixels = Height  * Width


def getFilenames(exts):
    fnames = [glob.glob(ext) for ext in exts]
    fnames = list(itertools.chain.from_iterable(fnames))
    return fnames

labels = {"NORMAL":0, "PNEUMONIA":1}


# get `.png` in  current folder and subfolders
exts = ["*.png","*/*.jpeg"]
res = getFilenames(exts)

buffer = []


# Creates new bin file in directory
with open('train_batch.bin', 'wb') as fp:
    pass


out = bytearray(buffer)
with open("train_batch.bin", "r+b") as out_file:
    out_file.write(out)

    count = 0
    for i in res:
        img_old = cv2.imread(i)
        label = (i.split("/"))[0]

        # Resize the images
        img = cv2.resize(img_old, (Width, Height))
        
        # Go through image and convert it to binary format
        binImgR = []
        binImgG = []
        binImgB = []

        for j in range(Height):
            for i in range(Width):
                (b, g, r) = img[j, i] # x = i, y = j
                binImgR.append(r)
                binImgG.append(g)
                binImgB.append(b)
        
                
        buffer.extend([labels[label]] + binImgR + binImgG + binImgB)
        count+=1

        # Write to file and empty buffer
        if (count % 256 == 0):
            out = bytearray(buffer)
            out_file.write(out)

            buffer = []
            print("Done with %d images", count)
            
        
    # After done with for loop, append remaining bytes
    out = bytearray(buffer)
    out_file.write(out)
    
    


