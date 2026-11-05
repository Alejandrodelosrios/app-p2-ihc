export type PetSpecies =
  | 'DOG'
  | 'CAT'
  | 'BIRD'
  | 'RABBIT'
  | 'RODENT'
  | 'REPTILE'
  | 'FISH'
  | 'OTHER';

export interface Pet {
  id: string;
  ownerId: string;
  name: string;
  species: PetSpecies;
  breed?: string | null;
  birthDate?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface SavePetRequest {
  name: string;
  species: PetSpecies;
  breed?: string | null;
  birthDate?: string | null;
}
